document.addEventListener("DOMContentLoaded", function () {
    const table = document.getElementById("table-bassins");
    if (!table) return;

    const tbody = table.querySelector("tbody");
    const emptyFiltered = document.getElementById("row-empty-filtered");
    const emptyServer = document.getElementById("row-empty-server");
    const rowCount = document.getElementById("row-count");

    const inputSearch = document.getElementById("search-global");
    const selectStatut = document.getElementById("filter-statut");
    const inputSurfaceMin = document.getElementById("filter-surface-min");
    const inputSurfaceMax = document.getElementById("filter-surface-max");
    const btnReset = document.getElementById("btn-reset-filters");

    // Les vraies lignes de données (on exclut la ligne "vide" gérée par le serveur)
    function getDataRows() {
        return Array.from(tbody.querySelectorAll("tr")).filter(
            (tr) => tr.id !== "row-empty-server"
        );
    }

    let currentSort = { key: "code", type: "text", dir: "asc" };

    function normalize(str) {
        return (str || "")
            .toString()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toLowerCase();
    }

    function applyFilters() {
        const rows = getDataRows();
        const search = normalize(inputSearch.value.trim());
        const statut = selectStatut.value;
        const min = inputSurfaceMin.value !== "" ? parseFloat(inputSurfaceMin.value) : null;
        const max = inputSurfaceMax.value !== "" ? parseFloat(inputSurfaceMax.value) : null;

        let visibleCount = 0;

        rows.forEach((row) => {
            const code = row.dataset.code || "";
            const surface = parseFloat(row.dataset.surface) || 0;
            const profondeur = row.dataset.profondeur || "";
            const rowStatut = row.dataset.statut || "";

            let visible = true;

            // Recherche multicritère (code, surface, profondeur)
            if (search) {
                const haystack = normalize(code + " " + surface + " " + profondeur);
                if (!haystack.includes(search)) visible = false;
            }

            // Filtre par colonne : statut
            if (visible && statut && rowStatut !== statut) {
                visible = false;
            }

            // Filtre par colonne : surface min/max
            if (visible && min !== null && surface < min) visible = false;
            if (visible && max !== null && surface > max) visible = false;

            row.style.display = visible ? "" : "none";
            if (visible) visibleCount++;
        });

        emptyFiltered.style.display = visibleCount === 0 && rows.length > 0 ? "" : "none";
        rowCount.textContent = rows.length > 0 ? visibleCount + " bassin(s) affiché(s) sur " + rows.length : "";
    }

    function sortRows(key, type) {
        const rows = getDataRows();
        const dir = currentSort.key === key && currentSort.dir === "asc" ? "desc" : "asc";
        currentSort = { key, type, dir };

        const datasetKey = key === "code" ? "code" : key === "surface" ? "surface" : key === "profondeur" ? "profondeur" : "statut";

        rows.sort((a, b) => {
            let va = a.dataset[datasetKey];
            let vb = b.dataset[datasetKey];

            if (type === "number") {
                va = parseFloat(va) || 0;
                vb = parseFloat(vb) || 0;
                return dir === "asc" ? va - vb : vb - va;
            } else {
                va = normalize(va);
                vb = normalize(vb);
                if (va < vb) return dir === "asc" ? -1 : 1;
                if (va > vb) return dir === "asc" ? 1 : -1;
                return 0;
            }
        });

        rows.forEach((row) => tbody.appendChild(row));

        table.querySelectorAll("th.sortable").forEach((th) => {
            th.classList.remove("sort-asc", "sort-desc");
            if (th.dataset.sortKey === key) {
                th.classList.add(dir === "asc" ? "sort-asc" : "sort-desc");
            }
        });
    }

    // Tri croissant par code au chargement (comportement par défaut demandé)
    sortRows("code", "text");

    table.querySelectorAll("th.sortable").forEach((th) => {
        th.addEventListener("click", () => {
            sortRows(th.dataset.sortKey, th.dataset.sortType);
        });
    });

    [inputSearch, selectStatut, inputSurfaceMin, inputSurfaceMax].forEach((el) => {
        el.addEventListener("input", applyFilters);
        el.addEventListener("change", applyFilters);
    });

    btnReset.addEventListener("click", () => {
        inputSearch.value = "";
        selectStatut.value = "";
        inputSurfaceMin.value = "";
        inputSurfaceMax.value = "";
        applyFilters();
    });

    applyFilters();
});