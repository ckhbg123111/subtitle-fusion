package com.zhongjia.subtitlefusion.ffmpeg.transition;

import com.zhongjia.subtitlefusion.util.MediaProbeUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用 xfade 的 zoom 转场 + acrossfade 音频。
 */
public class XfadeZoomTransitionStrategy implements TransitionStrategy {

    @Override
    public String[] buildCommand(List<Path> inputs, double t, Path output) throws Exception {
        if (inputs == null || inputs.size() == 0) throw new IllegalArgumentException("inputs is empty");
        if (inputs.size() == 1) {
            // 单输入直接拷贝
            return new String[]{
                    "ffmpeg", "-y",
                    "-i", inputs.get(0).toString(),
                    "-c", "copy",
                    output.toString()
            };
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
        // 统一视频像素格式、音频格式；并为无音轨段创建静音
        for (int i = 0; i < inputs.size(); i++) {
            fc.append("[").append(i).append(":v]")
              .append("format=yuv420p,setsar=1")
              .append("[v").append(i).append("]; ");
            if (hasAud[i]) {
                fc.append("[").append(i).append(":a]")
                  .append("aformat=fltp:44100:stereo")
                  .append("[a").append(i).append("]; ");
            } else {
                // 用 anullsrc 生成与该段时长相同的静音
                fc.append("anullsrc=channel_layout=stereo:sample_rate=44100,atrim=0:")
                  .append(String.format(java.util.Locale.US, "%.3f", Math.max(0.0, durs[i])))
                  .append(",asetpts=N/SR/TB[a").append(i).append("]; ");
            }
        }

        String vPrev = "v0"; String aPrev = "a0";
        double sum = durs[0];
        for (int i = 1; i < inputs.size(); i++) {
            double offset = Math.max(0d, sum - t);
            String vOut = "vX" + i;
            String aOut = "aX" + i;
            fc.append("[").append(vPrev).append("][v").append(i).append("]")
              .append("xfade=transition=fade:duration=")
              .append(String.format(java.util.Locale.US, "%.3f", t))
              .append(":offset=")
              .append(String.format(java.util.Locale.US, "%.3f", offset))
              .append("[").append(vOut).append("]; ");
            fc.append("[").append(aPrev).append("][a").append(i).append("]")
              .append("acrossfade=d=")
              .append(String.format(java.util.Locale.US, "%.3f", t))
              .append("[").append(aOut).append("]; ");
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

    @Override
    public String name() { return "zoom"; }
}


