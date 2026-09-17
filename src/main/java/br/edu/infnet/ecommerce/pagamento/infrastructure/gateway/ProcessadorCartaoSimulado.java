package br.edu.infnet.ecommerce.pagamento.infrastructure.gateway;

import br.edu.infnet.ecommerce.pagamento.domain.Dinheiro;
import br.edu.infnet.ecommerce.pagamento.domain.NumeroCartao;
import br.edu.infnet.ecommerce.pagamento.domain.port.ProcessadorCartao;
import br.edu.infnet.ecommerce.pagamento.domain.port.ResultadoProcessadorCartao;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProcessadorCartaoSimulado implements ProcessadorCartao {

    @Override
    public ResultadoProcessadorCartao processar(Dinheiro valor, NumeroCartao numeroCartao) {
        if (numeroCartao.terminaCom("1111")) {
            return ResultadoProcessadorCartao.aprovado(gerarCodigoAutorizacao());
        }

        return ResultadoProcessadorCartao.aprovado(gerarCodigoAutorizacao());
    }

    private String gerarCodigoAutorizacao() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
