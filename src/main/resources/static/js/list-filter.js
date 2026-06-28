/**
 * Filtre client générique pour tableaux HTML.
 *
 * Usage minimal :
 *   <input data-list-filter="#ma-table" placeholder="Rechercher...">
 *   <table id="ma-table"> ... <tbody> ... </tbody> </table>
 *
 * Optionnel : data-cols="0,2,3" pour ne filtrer que sur certaines colonnes.
 */
(function () {
  function applyFilter(input) {
    const sel = input.getAttribute('data-list-filter');
    const table = document.querySelector(sel);
    if (!table) return;

    const cols = (input.getAttribute('data-cols') || '')
      .split(',').map(s => s.trim()).filter(Boolean).map(Number);

    const query = input.value.trim().toLowerCase();
    const rows = table.tBodies[0] ? table.tBodies[0].rows : table.rows;

    for (const row of rows) {
      if (!query) { row.style.display = ''; continue; }
      const cells = cols.length
        ? cols.map(i => row.cells[i]).filter(Boolean)
        : Array.from(row.cells);
      const txt = cells.map(c => (c.textContent || '').toLowerCase()).join(' ');
      row.style.display = txt.includes(query) ? '' : 'none';
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-list-filter]').forEach(input => {
      input.addEventListener('input', () => applyFilter(input));
    });
  });
})();
