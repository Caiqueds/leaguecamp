// Botão "Copiar SQL" da página de consultas
const botaoCopiar = document.getElementById('copiarSql');

if (botaoCopiar) {
    botaoCopiar.addEventListener('click', function () {
        const sql = document.getElementById('textoSql').textContent;
        navigator.clipboard.writeText(sql).then(function () {
            botaoCopiar.textContent = 'SQL copiado';
            setTimeout(function () {
                botaoCopiar.textContent = 'Copiar SQL';
            }, 1500);
        });
    });
}
