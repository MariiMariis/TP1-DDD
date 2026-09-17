package br.edu.infnet.ecommerce.pagamento.domain.port;

import br.edu.infnet.ecommerce.pagamento.domain.Dinheiro;
import br.edu.infnet.ecommerce.pagamento.domain.NumeroCartao;

public interface ProcessadorCartao {
    ResultadoProcessadorCartao processar(Dinheiro valor, NumeroCartao numeroCartao);
}
