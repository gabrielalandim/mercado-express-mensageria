package br.com.fiap.mercadoexpress.loja.services;

import br.com.fiap.mercadoexpress.loja.models.EmailEnviado;
import br.com.fiap.mercadoexpress.loja.models.Pedido;
import br.com.fiap.mercadoexpress.loja.models.StatusEmail;
import br.com.fiap.mercadoexpress.loja.repositories.EmailEnviadoRepository;
import br.com.fiap.mercadoexpress.loja.repositories.PedidoRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final PedidoRepository pedidoRepository;
    private final EmailEnviadoRepository emailRepository;

    @Value("${app.mail.from}")
    private String remetente;

    @Transactional
    public Optional<EmailEnviado> enviarConfirmacaoPagamento(Long pedidoId) {
        Optional<Pedido> encontrado = pedidoRepository.findById(pedidoId);
        if (encontrado.isEmpty()) {
            log.warn("Pedido #{} não encontrado; e-mail não enviado.", pedidoId);
            return Optional.empty();
        }
        if (emailRepository.existsByPedidoIdAndStatus(pedidoId, StatusEmail.ENVIADO)) {
            log.info("Confirmação do pedido #{} já havia sido enviada.", pedidoId);
            return Optional.empty();
        }

        Pedido pedido = encontrado.get();
        String assunto = "Pagamento confirmado - Pedido #" + pedido.getId() + " | Mercado Express";

        Context ctx = new Context();
        ctx.setVariable("pedido", pedido);
        String html = templateEngine.process("email/confirmacao-pagamento", ctx);

        EmailEnviado registro = new EmailEnviado();
        registro.setPedidoId(pedido.getId());
        registro.setDestinatario(pedido.getClienteEmail());
        registro.setAssunto(assunto);
        registro.setCorpo(html);
        registro.setEnviadoEm(LocalDateTime.now());

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(remetente, "Mercado Express");
            helper.setTo(pedido.getClienteEmail());
            helper.setSubject(assunto);
            helper.setText(html, true);
            mailSender.send(mime);

            registro.setStatus(StatusEmail.ENVIADO);
            log.info("E-mail de confirmação enviado para {} (pedido #{}).", pedido.getClienteEmail(), pedido.getId());
        } catch (Exception e) {
            registro.setStatus(StatusEmail.FALHA);
            String erro = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            registro.setErro(erro.length() > 500 ? erro.substring(0, 500) : erro);
            log.error("Falha ao enviar e-mail do pedido #{}: {}", pedido.getId(), erro);
        }

        return Optional.of(emailRepository.save(registro));
    }

    @Transactional(readOnly = true)
    public List<EmailEnviado> listarTodos() {
        return emailRepository.findAllByOrderByEnviadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<EmailEnviado> listarPorPedido(Long pedidoId) {
        return emailRepository.findByPedidoIdOrderByEnviadoEmDesc(pedidoId);
    }
}
