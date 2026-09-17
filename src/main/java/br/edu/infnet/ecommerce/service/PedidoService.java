package br.edu.infnet.ecommerce.service;

import br.edu.infnet.ecommerce.entity.Estoque;
import br.edu.infnet.ecommerce.entity.ItemPedido;
import br.edu.infnet.ecommerce.entity.Pedido;
import br.edu.infnet.ecommerce.entity.Produto;
import br.edu.infnet.ecommerce.entity.Usuario;
import br.edu.infnet.ecommerce.exception.EstoqueInsuficienteException;
import br.edu.infnet.ecommerce.exception.PagamentoRecusadoException;
import br.edu.infnet.ecommerce.exception.RecursoNaoEncontradoException;
import br.edu.infnet.ecommerce.pagamento.application.ProcessarPagamentoCommand;
import br.edu.infnet.ecommerce.pagamento.application.ResultadoPagamento;
import br.edu.infnet.ecommerce.pagamento.integration.PagamentoFacade;
import br.edu.infnet.ecommerce.repository.EstoqueRepository;
import br.edu.infnet.ecommerce.repository.PedidoRepository;
import br.edu.infnet.ecommerce.repository.ProdutoRepository;
import br.edu.infnet.ecommerce.repository.UsuarioRepository;
import br.edu.infnet.ecommerce.request.CriarPedidoRequest;
import br.edu.infnet.ecommerce.request.ItemPedidoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final PedidoRepository pedidoRepository;
    private final PagamentoFacade pagamentoFacade;

    public PedidoService(
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            EstoqueRepository estoqueRepository,
            PedidoRepository pedidoRepository,
            PagamentoFacade pagamentoFacade
    ) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagamentoFacade = pagamentoFacade;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido não encontrado: " + id
                ));
    }

    @Transactional
    public Pedido criar(CriarPedidoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado: " + request.usuarioId()
                ));

        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        Pedido pedido = new Pedido(usuario);
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: " + itemRequest.produtoId()
                    ));

            if (!produto.isAtivo()) {
                throw new IllegalArgumentException(
                        "Produto inativo: " + produto.getNome()
                );
            }

            Estoque estoque = estoqueRepository.findByProdutoId(produto.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Estoque não encontrado para o produto: " + produto.getId()
                    ));

            if (estoque.getQuantidade() < itemRequest.quantidade()) {
                throw new EstoqueInsuficienteException(
                        "Estoque insuficiente para o produto: " + produto.getNome()
                );
            }

            estoque.setQuantidade(
                    estoque.getQuantidade() - itemRequest.quantidade()
            );
            estoqueRepository.save(estoque);

            ItemPedido item = new ItemPedido(
                    produto,
                    itemRequest.quantidade(),
                    produto.getPreco()
            );

            pedido.adicionarItem(item);
            total = total.add(item.getSubtotal());
        }

        pedido.setValorTotal(total);
        pedido.setStatus("AGUARDANDO_PAGAMENTO");
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        ProcessarPagamentoCommand command = new ProcessarPagamentoCommand(
                pedidoSalvo.getId(),
                usuario.getId(),
                total,
                request.formaPagamento(),
                request.numeroCartao()
        );

        ResultadoPagamento resultado = pagamentoFacade.processar(command);

        if (!resultado.aprovado()) {
            pedidoSalvo.setStatus("PAGAMENTO_RECUSADO");
            pedidoRepository.save(pedidoSalvo);

            throw new PagamentoRecusadoException(
                    "Pagamento recusado: " + resultado.motivo()
            );
        }

        pedidoSalvo.setStatus("PAGO");
        return pedidoRepository.save(pedidoSalvo);
    }
}
