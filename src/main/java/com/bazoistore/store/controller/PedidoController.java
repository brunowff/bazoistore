package com.bazoistore.store.controller;

import com.bazoistore.store.model.Pedido;
import com.bazoistore.store.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository repository;

    public PedidoController(PedidoRepository repository) {
        this.repository = repository;
    }

    // POST /pedidos - Cria um novo pedido
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido) {
        Pedido salvo = repository.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // GET /pedidos - Lista todos os pedidos
    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    // GET /pedidos/{id} - Busca pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        Optional<Pedido> pedido = repository.findById(id);
        return pedido.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    // PUT /pedidos/{id} - Atualiza um pedido existente
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> atualizar(@PathVariable Long id, @RequestBody Pedido dados) {
        return repository.findById(id).map(pedido -> {
            pedido.setClienteId(dados.getClienteId());
            pedido.setProdutoId(dados.getProdutoId());
            pedido.setQuantidade(dados.getQuantidade());
            return ResponseEntity.ok(repository.save(pedido));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /pedidos/{id} - Remove um pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
