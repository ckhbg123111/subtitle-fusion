package com.zhongjia.subtitlefusion.ffmpeg.transition;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.util.MediaProbeUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用 xfade 转场（默认 zoomin）+ acrossfade 音频。
 */
public class XfadeTransitionStrategy implements TransitionStrategy {

    @Override
    public String[] buildCommand(List<Path> inputs, List<VideoChainRequest.GapTransitionSpec> gaps, Path output) throws Exception {
        if (inputs == null || inputs.size() == 0) throw new IllegalArgumentException("inputs is empty");
        if (inputs.size() == 1) {
            return new String[]{
                    "ffmpeg", "-y",
                    "-i", inputs.get(0).toString(),
                    "-c", "copy",
                    output.toString()
            };
        }

        // gaps 为 null：严格按需求，不加任何转场动效
        if (gaps == null) {
            List<String> cmd = new ArrayList<>();
            cmd.add("ffmpeg"); cmd.add("-y");
            for (Path p : inputs) { cmd.add("-i"); cmd.add(p.toString()); }

            double[] durs = new double[inputs.size()];
            boolean[] hasAud = new boolean[inputs.size()];
            for (int i = 0; i < inputs.size(); i++) {
                durs[i] = MediaProbeUtils.probeDurationSeconds(inputs.get(i));
                hasAud[i] = MediaProbeUtils.hasAudioStream(inputs.get(i));
            }

            StringBuilder fc = new StringBuilder();
            for (int i = 0; i < inputs.size(); i++) {
                fc.append("[").append(i).append(":v]")
                  .append("format=yuv420p,setsar=1")
                  .append("[v").append(i).append("]; ");
                if (hasAud[i]) {
                    fc.append("[").append(i).append(":a]")
                      .append("aformat=fltp:44100:stereo")
                      .append("[a").append(i).append("]; ");
                } else {
                    fc.append("anullsrc=channel_layout=stereo:sample_rate=44100,atrim=0:")
                      .append(String.format(java.util.Locale.US, "%.3f", Math.max(0.0, durs[i])))
                      .append(",asetpts=N/SR/TB[a").append(i).append("]; ");
                }
            }
            for (int i = 0; i < inputs.size(); i++) {
                fc.append("[v").append(i).append("][a").append(i).append("]");
            }
            fc.append("concat=n=").append(inputs.size()).append(":v=1:a=1[outv][outa]");
            cmd.add("-filter_complex"); cmd.add(fc.toString());
            cmd.add("-map"); cmd.add("[outv]");
            cmd.add("-map"); cmd.add("[outa]");
            cmd.add("-c:v"); cmd.add("libx264");
            cmd.add("-c:a"); cmd.add("aac");
            cmd.add("-movflags"); cmd.add("+faststart");
            cmd.add(output.toString());
            return cmd.toArray(new String[0]);
        }

        if (gaps.size() != inputs.size() - 1) {
            throw new IllegalArgumentException("gaps size must be inputs.size()-1 when provided");
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg"); cmd.add("-y");
        for (Path p : inputs) { cmd.add("-i"); cmd.add(p.toString()); }

        double[] durs = new double[inputs.size()];
        boolean[] hasAud = new boolean[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            durs[i] = MediaProbeUtils.probeDurationSeconds(inputs.get(i));
            hasAud[i] = MediaProbeUtils.hasAudioStream(inputs.get(i));
        }

        StringBuilder fc = new StringBuilder();
        for (int i = 0; i < inputs.size(); i++) {
            fc.append("[").append(i).append(":v]")
              .append("format=yuv420p,setsar=1")
              .append("[v").append(i).append("]; ");
            if (hasAud[i]) {
                fc.append("[").append(i).append(":a]")
                  .append("aformat=fltp:44100:stereo")
                  .append("[a").append(i).append("]; ");
            } else {
                fc.append("anullsrc=channel_layout=stereo:sample_rate=44100,atrim=0:")
                  .append(String.format(java.util.Locale.US, "%.3f", Math.max(0.0, durs[i])))
                  .append(",asetpts=N/SR/TB[a").append(i).append("]; ");
            }
        }

        String vPrev = "v0"; String aPrev = "a0";
        double sum = durs[0];
        for (int i = 1; i < inputs.size(); i++) {
            VideoChainRequest.GapTransitionSpec spec = gaps.get(i - 1);
            String vOut = "vO" + i;
            String aOut = "aO" + i;
            if (spec == null) {
                // 直接拼接该两段
                fc.append("[").append(vPrev).append("][v").append(i).append("]")
                  .append("[").append(aPrev).append("][a").append(i).append("]")
                  .append("concat=n=2:v=1:a=1[").append(vOut).append("][").append(aOut).append("]; ");
            } else {
                if (spec.getType() == null || spec.getDurationSec() == null || spec.getDurationSec() <= 0) {
                    // 配置不完整，退化为直接拼接
                    fc.append("[").append(vPrev).append("][v").append(i).append("]")
                      .append("[").append(aPrev).append("][a").append(i).append("]")
                      .append("concat=n=2:v=1:a=1[").append(vOut).append("][").append(aOut).append("]; ");
                } else {
                    double t = Math.max(0.0, spec.getDurationSec());
                    double offset = Math.max(0d, sum - t);
                    String trans = toTransitionName(spec.getType());
                    fc.append("[").append(vPrev).append("][v").append(i).append("]")
                      .append("xfade=transition=").append(trans)
                      .append(":duration=").append(String.format(java.util.Locale.US, "%.3f", t))
                      .append(":offset=").append(String.format(java.util.Locale.US, "%.3f", offset))
                      .append("[").append(vOut).append("]; ");
                    fc.append("[").append(aPrev).append("][a").append(i).append("]")
                      .append("acrossfade=d=").append(String.format(java.util.Locale.US, "%.3f", t))
                      .append("[").append(aOut).append("]; ");
                }
            }
            vPrev = vOut; aPrev = aOut; sum += durs[i];
        }

        cmd.add("-filter_complex"); cmd.add(fc.toString());
        cmd.add("-map"); cmd.add("[" + vPrev + "]");
        cmd.add("-map"); cmd.add("[" + aPrev + "]");
        cmd.add("-c:v"); cmd.add("libx264");
        cmd.add("-c:a"); cmd.add("aac");
        cmd.add("-movflags"); cmd.add("+faststart");
        cmd.add(output.toString());
        return cmd.toArray(new String[0]);
    }

    private String toTransitionName(VideoChainRequest.TransitionType t) {
        switch (t) {
            case CUSTOM: return "custom";
            case FADE: return "fade";
            case WIPELEFT: return "wipeleft";
            case WIPERIGHT: return "wiperight";
            case WIPEUP: return "wipeup";
            case WIPEDOWN: return "wipedown";
            case SLIDELEFT: return "slideleft";
            case SLIDERIGHT: return "slideright";
            case SLIDEUP: return "slideup";
            case SLIDEDOWN: return "slidedown";
            case CIRCLECROP: return "circlecrop";
            case RECTCROP: return "rectcrop";
            case DISTANCE: return "distance";
            case FADEBLACK: return "fadeblack";
            case FADEWHITE: return "fadewhite";
            case RADIAL: return "radial";
            case SMOOTHLEFT: return "smoothleft";
            case SMOOTHRIGHT: return "smoothright";
            case SMOOTHUP: return "smoothup";
            case SMOOTHDOWN: return "smoothdown";
            case CIRCLEOPEN: return "circleopen";
            case CIRCLECLOSE: return "circleclose";
            case VERTOPEN: return "vertopen";
            case VERTCLOSE: return "vertclose";
            case HORZOPEN: return "horzopen";
            case HORZCLOSE: return "horzclose";
            case DISSOLVE: return "dissolve";
            case PIXELIZE: return "pixelize";
            case DIAGTL: return "diagtl";
            case DIAGTR: return "diagtr";
            case DIAGBL: return "diagbl";
            case DIAGBR: return "diagbr";
            case HLSLICE: return "hlslice";
            case HRSLICE: return "hrslice";
            case VUSLICE: return "vuslice";
            case VDSLICE: return "vdslice";
            case HBLUR: return "hblur";
            case FADEGRAYS: return "fadegrays";
            case WIPETL: return "wipetl";
            case WIPETR: return "wipetr";
            case WIPEBL: return "wipebl";
            case WIPEBR: return "wipebr";
            case SQUEEZEH: return "squeezeh";
            case SQUEEZEV: return "squeezev";
            case ZOOMIN: return "zoomin";
            case FADEFAST: return "fadefast";
            case FADESLOW: return "fadeslow";
            case HLWIND: return "hlwind";
            case HRWIND: return "hrwind";
            case VUWIND: return "vuwind";
            case VDWIND: return "vdwind";
            case COVERLEFT: return "coverleft";
            case COVERRIGHT: return "coverright";
            case COVERUP: return "coverup";
            case COVERDOWN: return "coverdown";
            case REVEALLEFT: return "revealleft";
            case REVEALRIGHT: return "revealright";
            case REVEALUP: return "revealup";
            case REVEALDOWN: return "revealdown";
            default: return "";
        }
    }

    @Override
    public String name() { return "xfade"; }
}


