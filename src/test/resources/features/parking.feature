Feature: Gerenciamento de Estacionamento
  Como um sistema de estacionamento
  Eu quero gerenciar o estacionamento de veículos
  Para que eu possa rastrear e cobrar as atividades de estacionamento

  Contexto:
    Dado que o sistema de estacionamento está operacional
    E existe um setor "A" com preço base "10.00" e capacidade "100"

  Cenário: Veículo entra no estacionamento
    Quando um veículo com placa "ABC1234" entra às "2024-01-01T10:00:00"
    Então a entrada deve ser registrada
    E o veículo deve ser marcado como no estacionamento

  Cenário: Veículo estaciona em uma vaga disponível
    Dado que um veículo com placa "ABC1234" está no estacionamento
    Quando o veículo estaciona nas coordenadas "-23.561684, -46.655981"
    Então a vaga deve ser marcada como ocupada
    E a ocupação do setor deve aumentar em 1
    E o evento de estacionamento deve ser registrado

  Cenário: Veículo sai do estacionamento
    Dado que um veículo com placa "ABC1234" está estacionado nas coordenadas "-23.561684, -46.655981"
    Quando o veículo sai às "2024-01-01T12:00:00"
    Então a vaga deve ser marcada como disponível
    E a ocupação do setor deve diminuir em 1
    E o evento de saída deve ser registrado
    E a taxa de estacionamento deve ser calculada com base na duração

  Cenário: Tentativa de estacionar em vaga ocupada
    Dado que uma vaga nas coordenadas "-23.561684, -46.655981" está ocupada
    Quando outro veículo tenta estacionar na mesma vaga
    Então o sistema deve rejeitar a tentativa de estacionamento
    E lançar uma "SpotOccupiedException"

  Cenário: Verificar status de estacionamento do veículo
    Dado que um veículo com placa "ABC1234" está estacionado
    Quando eu verifico o status do veículo
    Então eu devo ver as informações atuais de estacionamento
    E o preço calculado com base na duração

  Cenário: Verificar status da vaga
    Dado que existe uma vaga nas coordenadas "-23.561684, -46.655981"
    Quando eu verifico o status da vaga
    Então eu devo ver se a vaga está ocupada
    E as informações do veículo se estiver ocupada

  Cenário: Calcular preço dinâmico com baixa ocupação
    Dado que o setor "A" tem 20% de ocupação
    Quando um veículo estaciona por 2 horas
    Então o preço deve ser calculado com 10% de desconto

  Cenário: Calcular preço dinâmico com alta ocupação
    Dado que o setor "A" tem 80% de ocupação
    Quando um veículo estaciona por 2 horas
    Então o preço deve ser calculado com 25% de aumento 