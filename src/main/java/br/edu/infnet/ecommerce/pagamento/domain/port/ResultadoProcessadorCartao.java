package br.edu.infnet.ecommerce.pagamento.domain.port;

public record ResultadoProcessadorCartao(
        boolean aprovado,
        String motivo,
        String codigoAutorizacao
) {
    public static ResultadoProcessadorCartao aprovado(String codigoAutorizacao) {
        return new ResultadoProcessadorCartao(true, null, codigoAutorizacao);
    }

    public static ResultadoProcessadorCartao recusado(String motivo) {
        return new ResultadoProcessadorCartao(false, motivo, null);
    }
}
