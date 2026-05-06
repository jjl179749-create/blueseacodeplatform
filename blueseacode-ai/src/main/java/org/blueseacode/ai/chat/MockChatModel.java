package org.blueseacode.ai.chat;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 开发模式 Mock 模型
 * 当 volc-engine.api-key=mock 时使用
 */
@Slf4j
public class MockChatModel implements ChatModel {

    @Override
    public ChatResponse chat(ChatRequest request) {
        String question = ((UserMessage) request.messages().get(0)).singleText();
        String answer = getMockAnswer(question);
        log.debug("[MockAI] Q: {} → A: {}", question, answer);
        return ChatResponse.builder()
                .aiMessage(dev.langchain4j.data.message.AiMessage.from(answer))
                .build();
    }

    private String getMockAnswer(String question) {
        // 积分相关
        if (question.contains("积分") || question.contains("score")) {
            return "积分可以通过以下方式获取：\n" +
                    "1. 每日登录签到 +10分\n" +
                    "2. 发布资源审核通过 +20分\n" +
                    "3. 发布文章审核通过 +15分\n" +
                    "4. 资源被下载每次 +2分\n" +
                    "5. 每日刷题打卡 +5分\n\n" +
                    "积分可用于下载付费资源。";
        }
        // 发布相关
        if (question.contains("发布") || question.contains("publish") || question.contains("上传")) {
            return "发布资源/文章的流程：\n" +
                    "1. 点击顶部导航「资源分享」或「编程提升」\n" +
                    "2. 填写标题、描述、上传文件/编写内容\n" +
                    "3. 提交后等待审核（通常24小时内）\n" +
                    "4. 审核通过后即可展示在平台\n\n" +
                    "发布需求同理，进入「需求发布」页面即可。";
        }
        // 注册登录相关
        if (question.contains("注册") || question.contains("登录") || question.contains("账号")) {
            return "注册和登录非常简单：\n" +
                    "1. 点击右上角「注册」按钮\n" +
                    "2. 填写用户名、邮箱、密码完成注册\n" +
                    "3. 注册后使用账号密码登录\n" +
                    "4. 登录后即可使用平台全部功能\n\n" +
                    "如忘记密码，请联系管理员重置。";
        }
        // 需求相关
        if (question.contains("需求") || question.contains("接单") || question.contains("demand")) {
            return "需求市场使用说明：\n" +
                    "1. 发布需求：填写标题、描述、预算等，提交后等待审核\n" +
                    "2. 接单：浏览招募中的需求，点击「立即接单」\n" +
                    "3. 一个需求只能被一人接单，先到先得\n" +
                    "4. 完成后由发布者确认完成\n\n" +
                    "注意：不能接自己的需求。";
        }
        // 默认回复
        return "您好！我是蓝海编程平台的智能客服小蓝。我可以帮您解答以下问题：\n\n" +
                "• 平台使用问题（注册、登录、发布、下载等）\n" +
                "• 积分获取与消耗规则\n" +
                "• 资源分享与下载流程\n" +
                "• 文章发布与阅读\n" +
                "• 需求发布与接单流程\n" +
                "• 账号相关咨询\n\n" +
                "请问有什么可以帮您的？如果问题比较复杂，我可以为您创建工单转人工客服处理。";
    }
}
