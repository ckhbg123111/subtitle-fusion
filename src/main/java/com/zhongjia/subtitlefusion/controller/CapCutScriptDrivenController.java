package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.CapCutGenResponse;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.service.DraftWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capcut-script-driven")
@Slf4j
@RequiredArgsConstructor
public class CapCutScriptDrivenController {

    private final DraftWorkflowService draftWorkflowService;

    @PostMapping(value = "/capcut-gen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CapCutGenResponse submit(@RequestBody SubtitleFusionV2Request request)  {
        // 调用端暂时不传输花字效果和文字模板
        // todo 当没有keywords时，随机选择一个花字或者选择一个文字模板，花字和文字模板见MCP接口文档
        return draftWorkflowService.generateDraft(request);
    }


}
