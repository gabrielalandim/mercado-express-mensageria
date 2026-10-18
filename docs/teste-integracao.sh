#!/usr/bin/env bash
# Script de teste ponta a ponta: cria compras via API e confere o retorno do Banco Tranquilo.
# Uso: bash docs/teste-integracao.sh   (com os 2 serviços e o docker compose rodando)
set -euo pipefail
LOJA=${LOJA:-http://localhost:8081}
BANCO=${BANCO:-http://localhost:8082}
DIR="$(cd "$(dirname "$0")" && pwd)"

echo "== Produtos =="
curl -s "$LOJA/api/produtos" | head -c 400; echo -e "\n"

echo "== Compra APROVADA (conta 1001-0) =="
ID1=$(curl -s -X POST "$LOJA/api/compras" -H 'Content-Type: application/json' \
      -d @"$DIR/exemplos/compra-aprovada.json" | sed -E 's/.*"id":([0-9]+).*/\1/')
echo "pedido #$ID1 criado"

echo "== Compra RECUSADA (conta 2001-0) =="
ID2=$(curl -s -X POST "$LOJA/api/compras" -H 'Content-Type: application/json' \
      -d @"$DIR/exemplos/compra-recusada-saldo.json" | sed -E 's/.*"id":([0-9]+).*/\1/')
echo "pedido #$ID2 criado"

echo "Aguardando o Banco Tranquilo processar (5s)..."; sleep 5

for ID in $ID1 $ID2; do
  echo "== Pedido #$ID =="
  curl -s "$LOJA/api/compras/$ID"; echo
done

echo "== E-mails do pedido #$ID1 =="
curl -s "$LOJA/api/compras/$ID1/emails"; echo

echo "== Transações no Banco Tranquilo =="
curl -s "$BANCO/api/transacoes" | head -c 600; echo
