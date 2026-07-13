/**
 * Synchronise les liens/boutons d'export (data-export-link) avec les filtres
 * actifs de la page (marqués data-filter-param="nomDuParamServeur") au moment
 * du clic, pour que l'export ne contienne que les lignes visibles selon le
 * filtre courant plutôt que toutes les données.
 *
 * Usage :
 *   <select data-filter-param="etat"> ... </select>
 *   <input data-filter-param="q" data-list-filter="#table" />
 *   <a data-export-link href="/xxx/export?format=excel">Excel</a>
 */
(function () {
  function paramsActuels() {
    var params = new URLSearchParams();
    document.querySelectorAll('[data-filter-param]').forEach(function (el) {
      var cle = el.getAttribute('data-filter-param');
      var val = (el.value || '').trim();
      if (val) params.set(cle, val);
    });
    return params;
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-export-link]').forEach(function (lien) {
      lien.addEventListener('click', function () {
        var url = new URL(lien.href, window.location.origin);
        paramsActuels().forEach(function (v, k) { url.searchParams.set(k, v); });
        lien.href = url.toString();
      });
    });
  });
})();
