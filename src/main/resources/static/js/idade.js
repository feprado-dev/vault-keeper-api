// Aviso 18+: mostra o <dialog> na primeira visita e lembra da resposta no navegador.
// (localStorage pode falhar em abas anônimas: por isso o try/catch.)
(function () {
  var KEY = 'idade-confirmada'
  var dialog = document.getElementById('idade')
  if (!dialog || typeof dialog.showModal !== 'function') return
  try {
    if (localStorage.getItem(KEY) === 'sim') return
  } catch (e) {}
  dialog.addEventListener('cancel', function (e) { e.preventDefault() }) // Esc não pula o aviso
  document.getElementById('idade-sim').addEventListener('click', function () {
    try { localStorage.setItem(KEY, 'sim') } catch (e) {}
    dialog.close()
  })
  dialog.showModal()
})()
