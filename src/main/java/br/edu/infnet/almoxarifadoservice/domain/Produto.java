package br.edu.infnet.almoxarifadoservice.domain;

import br.edu.infnet.almoxarifadoservice.commands.AtualizarEstoqueCommand;
import br.edu.infnet.almoxarifadoservice.commands.CriarProdutoCommand;
import br.edu.infnet.almoxarifadoservice.events.EstoqueAtualizadoEvent;
import br.edu.infnet.almoxarifadoservice.events.ProdutoCriadoEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Aggregate
@Entity
public class Produto {

    @AggregateIdentifier
    @Id
    private String id;
    private String nome;
    private String descricao;
    private double preco;
    private int quantidadeEmEstoque;

    @CommandHandler
    public Produto(CriarProdutoCommand comando) {
        AggregateLifecycle.apply(new ProdutoCriadoEvent(comando.id, comando.nome, comando.descricao, comando.preco, comando.quantidadeEmEstoque));
    }

    @EventSourcingHandler
    public void on(ProdutoCriadoEvent evento) {
        this.id = evento.getId();
        this.nome = evento.getNome();
        this.descricao = evento.getDescricao();
        this.preco = evento.getPreco();
        this.quantidadeEmEstoque = evento.getQuantidadeEmEstoque();
    }


    @CommandHandler
    public void handle(AtualizarEstoqueCommand comando) {
        if (comando.getTipoMovimentacao() == TipoMovimentacao.SAIDA && this.quantidadeEmEstoque < comando.getQuantidade()) {
            throw new IllegalStateException("Estoque insuficiente para a operação de saída.");
        }

        AggregateLifecycle.apply(new EstoqueAtualizadoEvent(comando.id, comando.quantidade, comando.tipoMovimentacao));
    }

    @EventSourcingHandler
    public void on(EstoqueAtualizadoEvent evento) {
        if (evento.getTipoMovimentacao() == TipoMovimentacao.ENTRADA) {
            this.quantidadeEmEstoque += evento.getQuantidade();
        } else if (evento.getTipoMovimentacao() == TipoMovimentacao.SAIDA) {
            this.quantidadeEmEstoque -= evento.getQuantidade();
        }
    }
}
