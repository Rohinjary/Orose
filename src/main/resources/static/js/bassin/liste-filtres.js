document.addEventListener("DOMContentLoaded", function () {
    const grid = document.getElementById("bassin-grid");
    if (!grid) return;

    const emptyFiltered = document.getElementById("empty-filtered");
    const rowCount = document.getElementById("row-count");

    const selectEtat = document.getElementById("filter-etat");
    const selectDisponibilite = document.getElementById("filter-disponibilite");
    const inputCycle = document.getElementById("filter-cycle");
    const btnReset = document.getElementById("btn-reset-filters");

    function getCards() {
        return Array.from(grid.querySelectorAll(".bassin-card"));
    }

    function normalize(str) {
        return (str || "").toString().toLowerCase()
            .replace(/[aàáâãäå]/g, "a")
            .replace(/[eéèêë]/g, "e")
            .replace(/[iìíîï]/g, "i")
            .replace(/[oòóôõö]/g, "o")
            .replace(/[uùúûü]/g, "u")
            .replace(/ç/g, "c");
    }

    function applyFilters() {
        const cards = getCards();
        const etat = selectEtat.value;
        const disponibilite = selectDisponibilite.value;
        const cycle = normalize(inputCycle.value.trim());

        let visibleCount = 0;

        cards.forEach((card) => {
            const cardEtat = card.dataset.statut || "";
            const cardDisponibilite = card.dataset.disponibilite || "";
            const cardCycle = normalize(card.dataset.cycle || "");

            let visible = true;

            if (etat && cardEtat !== etat) visible = false;
            if (visible && disponibilite && cardDisponibilite !== disponibilite) visible = false;
            if (visible && cycle && !cardCycle.includes(cycle)) visible = false;

            card.style.display = visible ? "" : "none";
            if (visible) visibleCount++;
        });

        if (emptyFiltered) {
            emptyFiltered.style.display = visibleCount === 0 && cards.length > 0 ? "" : "none";
        }
        if (rowCount) {
            rowCount.textContent = cards.length > 0
                ? visibleCount + " bassin(s) affiche(s) sur " + cards.length
                : "";
        }
    }

    [selectEtat, selectDisponibilite, inputCycle].forEach((el) => {
        el.addEventListener("input", applyFilters);
        el.addEventListener("change", applyFilters);
    });

    btnReset.addEventListener("click", () => {
        selectEtat.value = "";
        selectDisponibilite.value = "";
        inputCycle.value = "";
        applyFilters();
    });

    applyFilters();
});
