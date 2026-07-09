document.addEventListener('DOMContentLoaded', function () {
  new DataTable('.filtre-tab', {
    paging: true,
    pageLength: 5,
    // Configuration des choix de la liste déroulante
    // Le premier tableau contient les valeurs réelles [5, 10, 25, 50]
    // Le second tableau contient le texte affiché dans le select ["5", "10", "25", "50"]
    lengthMenu: [[5, 10, 25, 50], [5, 10, 25, 50]],
    searching: false,
    info: true,
    ordering: true,
    language: {
      // Traduction du "Show X entries" en haut à gauche
      lengthMenu: "Afficher _MENU_ éléments",
      info: "Affichage de l'élément _START_ à _END_ sur _TOTAL_ éléments",
      infoEmpty: "Affichage de 0 à 0 sur 0 élément",
      infoFiltered: "(filtré de _MAX_ éléments au total)",
      zeroRecords: "Aucun élément à afficher",
      emptyTable: "Aucune donnée disponible",
      paginate: {
        previous: "Précédent",
        next: "Suivant"
      }
    }
  });
});