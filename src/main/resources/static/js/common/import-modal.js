function ouvrirModalImport(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add("open");
}

function fermerModalImport(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove("open");
}

document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".import-modal-overlay").forEach((overlay) => {
        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) overlay.classList.remove("open");
        });
    });
});
