package com.itheima.mes1.common;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /** 邮件是否已配置 */
    private boolean isConfigured() {
        return mailSender != null && from != null && !from.isBlank();
    }

    /**
     * 发送验证码邮件
     */
    public void sendVerifyCode(String to, String code) {
        if (!isConfigured()) {
            log.warn("邮件未配置，跳过发送验证码 to={}", to);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【造易MES】邮箱验证码");

            String html = """
                    <div style="max-width:520px;margin:0 auto;font-family:'PingFang SC','Microsoft YaHei','Helvetica Neue',Arial,sans-serif;background:#ffffff;">
                      <!-- 头部 -->
                      <div style="background:linear-gradient(135deg, #1a73e8 0%%, #4a90d9 100%%);padding:30px 28px;border-radius:10px 10px 0 0;text-align:center;">
                        <h1 style="color:#fff;font-size:22px;font-weight:700;margin:0;letter-spacing:2px;">&#x1F3ED; 造易 MES 系统</h1>
                        <p style="color:rgba(255,255,255,0.9);font-size:13px;margin:8px 0 0;">智能制造执行系统 · 让生产更高效</p>
                      </div>
                      <!-- 正文 -->
                      <div style="border:1px solid #e8eaed;border-top:none;padding:32px 28px;border-radius:0 0 10px 10px;">
                        <p style="font-size:15px;color:#333;margin:0 0 6px;">您好，</p>
                        <p style="font-size:14px;color:#5f6368;margin:0 0 24px;line-height:1.8;">
                          您正在 <strong>造易 MES 系统</strong> 上进行账号注册操作，请使用以下验证码完成身份验证：
                        </p>
                        <!-- 验证码卡片 -->
                        <div style="background:linear-gradient(135deg, #f0f7ff 0%%, #e8f0fe 100%%);text-align:center;padding:20px 16px;border-radius:8px;border:1px dashed #1a73e8;margin:0 0 24px;">
                          <span style="font-size:36px;font-weight:800;color:#1a73e8;letter-spacing:8px;font-family:'Courier New',monospace;">%s</span>
                        </div>
                        <!-- 提示 -->
                        <div style="background:#fff8e1;border-left:4px solid #f9ab00;padding:12px 16px;border-radius:4px;margin-bottom:16px;">
                          <p style="font-size:13px;color:#e37400;margin:0;line-height:1.6;">
                            &#x26A0; 验证码 <strong>5 分钟内</strong>有效，请勿泄露给他人（包括自称客服的人员）。
                          </p>
                        </div>
                        <!-- 操作说明 -->
                        <table style="width:100%%;margin-bottom:16px;">
                          <tr>
                            <td style="vertical-align:top;width:32px;padding:6px 0;">
                              <span style="display:inline-block;width:22px;height:22px;background:#1a73e8;color:#fff;text-align:center;line-height:22px;border-radius:50%%;font-size:12px;font-weight:bold;">1</span>
                            </td>
                            <td style="font-size:13px;color:#5f6368;padding:6px 0;">返回注册页面，输入收到的 6 位验证码</td>
                          </tr>
                          <tr>
                            <td style="vertical-align:top;width:32px;padding:6px 0;">
                              <span style="display:inline-block;width:22px;height:22px;background:#1a73e8;color:#fff;text-align:center;line-height:22px;border-radius:50%%;font-size:12px;font-weight:bold;">2</span>
                            </td>
                            <td style="font-size:13px;color:#5f6368;padding:6px 0;">设置您的登录密码，完成注册</td>
                          </tr>
                          <tr>
                            <td style="vertical-align:top;width:32px;padding:6px 0;">
                              <span style="display:inline-block;width:22px;height:22px;background:#1a73e8;color:#fff;text-align:center;line-height:22px;border-radius:50%%;font-size:12px;font-weight:bold;">3</span>
                            </td>
                            <td style="font-size:13px;color:#5f6368;padding:6px 0;">使用注册邮箱和密码登录造易 MES 系统</td>
                          </tr>
                        </table>
                        <p style="font-size:12px;color:#9aa0a6;margin:0 0 24px;line-height:1.6;">
                          如果您没有注册造易 MES 账号，请忽略此邮件，您的邮箱不会被用于其他用途。
                        </p>
                      </div>
                      <!-- 页脚 -->
                      <div style="padding:20px 28px;text-align:center;">
                        <p style="font-size:12px;color:#bdc1c6;margin:0 0 4px;">
                          此邮件由系统自动发送，请勿回复。
                        </p>
                        <p style="font-size:12px;color:#bdc1c6;margin:0;">
                          &#x1F4E9; %s &#x00B7; 造易 MES 团队
                        </p>
                      </div>
                    </div>
                    """.formatted(code, to);

            helper.setText(html, true);
            mailSender.send(msg);
            log.info("验证码已发送至 {}", to);
        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    /** 发送通用 HTML 邮件 */
    public void sendHtml(String to, String subject, String html) {
        if (!isConfigured()) {
            log.warn("邮件未配置，跳过发送 subject={}", subject);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("邮件已发送至 {}", to);
        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    /** 发送带附件的 HTML 邮件 */
    public void sendHtmlWithAttachment(String to, String subject, String html,
                                        byte[] attachment, String filename) {
        if (!isConfigured()) {
            log.warn("邮件未配置，跳过发送附件邮件 subject={}", subject);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(subject);
            helper.setText(html, true);

            ByteArrayDataSource ds = new ByteArrayDataSource(attachment,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            helper.addAttachment(filename, ds);

            mailSender.send(msg);
            log.info("带附件邮件已发送至 {} filename={}", to, filename);
        } catch (MessagingException e) {
            log.error("带附件邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("带附件邮件发送失败: " + e.getMessage());
        }
    }
}
