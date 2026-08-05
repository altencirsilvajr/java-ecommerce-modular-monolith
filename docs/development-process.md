# Processo de desenvolvimento

O repositorio evolui por tracer bullets verticais e commits atomicos. Mudancas comportamentais começam por um teste em um seam publico, seguem pelo menor codigo que o satisfaz e terminam com verificacao proporcional ao risco.

Cada commit substantivo altera exatamente um registro em `journal/`. ADRs registram apenas decisoes duraveis e dificeis de reverter. O gate `scripts/verify-traceability.sh --staged` impede commits sem rastreabilidade.
