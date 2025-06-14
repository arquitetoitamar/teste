# language: pt
Funcionalidade: Gerenciamento de Estacionamento
  Como um operador do estacionamento
  Eu quero gerenciar a entrada e saída de veículos
  Para controlar o fluxo e ocupação do estacionamento

  Cenário: Veículo entra no estacionamento
    Dado que o sistema de estacionamento está operacional
    E existe um setor "A" com preço base "10.00" e capacidade "100"
    E um veículo com placa "ZUL0001"
    Quando o veículo entra no estacionamento às "2024-01-01T10:00:00"
    Então a entrada deve ser registrada

  Cenário: Veículo estaciona em uma vaga disponível
    Dado que o sistema de estacionamento está operacional
    E existe um setor "A" com preço base "10.00" e capacidade "100"
    E um veículo com placa "ZUL0001"
    E uma vaga nas coordenadas -23.561684 e -46.655981
    Quando o veículo estaciona na vaga com coordenadas -23.561684 e -46.655981
    #Então a vaga deve estar ocupada
    Então a placa do veículo na vaga deve ser "ZUL0001"

  Cenário: Tentar estacionar em vaga ocupada
    Dado que o sistema de estacionamento está operacional
    E existe um setor "A" com preço base "10.00" e capacidade "100"
    E um veículo com placa "ZUL0001"
    E uma vaga nas coordenadas -23.561684 e -46.655981
    E que um veículo com placa "XYZ789" está estacionado nas coordenadas "-23.561684, -46.655981"
    Quando tento estacionar o veículo na vaga com coordenadas -23.561684 e -46.655981

  Cenário: Veículo sai do estacionamento e valor é calculado
    Dado que o sistema de estacionamento está operacional
    E existe um setor "A" com preço base "10.00" e capacidade "100"
    E um veículo com placa "ZUL0001"
    E que o veículo entrou no estacionamento às "2024-01-01T10:00:00"
    E que o veículo está estacionado nas coordenadas -23.561684 e -46.655981
    Quando o veículo sai do estacionamento às "2024-01-01T12:00:00"
    Então o valor total a ser pago deve ser "9.00"
    E a vaga deve estar liberada
    E o veículo não deve mais estar no estacionamento
