package utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Composant calendrier personnalisé pour sélection de date
 * version ultra-compacte
 */
public class CalendarView extends VBox {
    private ArrayList<StackPane> calendarDayPanes = new ArrayList<>();
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private LocalDate today;
    private Label monthYearLabel;
    private GridPane calendarGrid;
    private EventHandler<ActionEvent> onDateSelectedHandler;
    private final Locale locale = Locale.FRENCH;
    
    /**
     * Constructeur du composant calendrier
     */
    public CalendarView() {
        this.getStyleClass().add("calendar-container");
        this.setSpacing(1);
        this.setPrefSize(240, 220); // Taille très réduite
        
        today = LocalDate.now();
        currentYearMonth = YearMonth.now();
        selectedDate = today;
        
        // Créer l'en-tête du calendrier
        createCalendarHeader();
        
        // Créer la grille du calendrier
        createCalendarGrid();
        
        // Initialiser le calendrier avec le mois actuel
        populateCalendar(currentYearMonth);
    }
    
    /**
     * Créer l'en-tête du calendrier avec les boutons de navigation
     */
    private void createCalendarHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("calendar-header");
        header.setPadding(new Insets(3, 0, 3, 0)); // Padding minimal
        
        // Bouton mois précédent
        Button prevButton = new Button("◀");
        prevButton.getStyleClass().add("calendar-nav-button");
        prevButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            populateCalendar(currentYearMonth);
        });
        
        // Affichage mois et année
        monthYearLabel = new Label();
        monthYearLabel.getStyleClass().add("calendar-header-label");
        HBox.setMargin(monthYearLabel, new Insets(0, 8, 0, 8)); // Marges minimales
        
        // Bouton mois suivant
        Button nextButton = new Button("▶");
        nextButton.getStyleClass().add("calendar-nav-button");
        nextButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            populateCalendar(currentYearMonth);
        });
        
        header.getChildren().addAll(prevButton, monthYearLabel, nextButton);
        this.getChildren().add(header);
    }
    
    /**
     * Créer la grille du calendrier avec les en-têtes des jours
     */
    private void createCalendarGrid() {
        // En-têtes des jours de la semaine
        GridPane dayLabels = new GridPane();
        dayLabels.setAlignment(Pos.CENTER);
        dayLabels.setPadding(new Insets(1, 0, 1, 0)); // Padding minimal
        
        // Ajouter les noms des jours de la semaine (lundi à dimanche)
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.of(i == 6 ? 7 : i + 1); // Lundi à Dimanche
            // Prendre juste la première lettre du jour
            String dayLetter = day.getDisplayName(TextStyle.NARROW, locale);
            Label dayLabel = new Label(dayLetter);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.getStyleClass().add("calendar-day-header");
            dayLabels.add(dayLabel, i, 0);
        }
        
        this.getChildren().add(dayLabels);
        
        // Grille des jours
        calendarGrid = new GridPane();
        calendarGrid.setPadding(new Insets(1)); // Padding minimal
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setHgap(1); // Espacement minimal
        calendarGrid.setVgap(1); // Espacement minimal
        
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                StackPane dayPane = new StackPane();
                dayPane.getStyleClass().add("calendar-day");
                dayPane.setPrefSize(28, 28); // Cellules très petites
                calendarDayPanes.add(dayPane);
                calendarGrid.add(dayPane, col, row);
            }
        }
        
        this.getChildren().add(calendarGrid);
    }
    
    /**
     * Remplit le calendrier avec les jours du mois spécifié
     */
    private void populateCalendar(YearMonth yearMonth) {
        // Mettre à jour le label du mois et de l'année
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yy", locale); // Format ultra-court
        monthYearLabel.setText(yearMonth.format(formatter));
        
        // Effacer les jours précédents
        for (StackPane dayPane : calendarDayPanes) {
            dayPane.getChildren().clear();
            dayPane.getStyleClass().remove("calendar-day-selected");
            dayPane.getStyleClass().remove("calendar-day-today");
            dayPane.getStyleClass().remove("calendar-day-inactive");
        }
        
        // Déterminer le jour de la semaine du 1er jour du mois
        int startDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue() - 1;
        if (startDayOfWeek == -1) startDayOfWeek = 6; // Dimanche
        
        // Remplir les jours du mois précédent (inactifs)
        YearMonth prevMonth = yearMonth.minusMonths(1);
        int prevMonthDays = prevMonth.lengthOfMonth();
        for (int i = 0; i < startDayOfWeek; i++) {
            int day = prevMonthDays - startDayOfWeek + i + 1;
            Label label = new Label(String.valueOf(day));
            label.setStyle("-fx-font-size: 9px;"); // Taille minimale
            
            StackPane dayPane = calendarDayPanes.get(i);
            dayPane.getStyleClass().add("calendar-day-inactive");
            dayPane.getChildren().add(label);
            
            // Date du mois précédent
            LocalDate date = prevMonth.atDay(day);
            setupDayCell(dayPane, date);
        }
        
        // Remplir les jours du mois actuel
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            Label label = new Label(String.valueOf(i));
            label.setStyle("-fx-font-size: 10px;"); // Taille minimale
            
            LocalDate date = yearMonth.atDay(i);
            int index = startDayOfWeek + i - 1;
            StackPane dayPane = calendarDayPanes.get(index);
            
            // Marquer le jour actuel
            if (date.equals(today)) {
                dayPane.getStyleClass().add("calendar-day-today");
            }
            
            // Marquer le jour sélectionné
            if (date.equals(selectedDate)) {
                dayPane.getStyleClass().add("calendar-day-selected");
            }
            
            dayPane.getChildren().add(label);
            setupDayCell(dayPane, date);
        }
        
        // Remplir les jours du mois suivant (inactifs)
        int nextMonthDays = 42 - (startDayOfWeek + daysInMonth); // 42 = 6 rows * 7 columns
        YearMonth nextMonth = yearMonth.plusMonths(1);
        for (int i = 0; i < nextMonthDays; i++) {
            int day = i + 1;
            Label label = new Label(String.valueOf(day));
            label.setStyle("-fx-font-size: 9px;"); // Taille minimale
            
            int index = startDayOfWeek + daysInMonth + i;
            StackPane dayPane = calendarDayPanes.get(index);
            dayPane.getStyleClass().add("calendar-day-inactive");
            dayPane.getChildren().add(label);
            
            // Date du mois suivant
            LocalDate date = nextMonth.atDay(day);
            setupDayCell(dayPane, date);
        }
    }
    
    /**
     * Configure une cellule de jour avec les actions et styles appropriés
     */
    private void setupDayCell(StackPane dayPane, LocalDate date) {
        dayPane.setOnMouseClicked(e -> {
            // Désélectionner la date précédente
            for (StackPane pane : calendarDayPanes) {
                pane.getStyleClass().remove("calendar-day-selected");
            }
            
            // Sélectionner la nouvelle date
            dayPane.getStyleClass().add("calendar-day-selected");
            selectedDate = date;
            
            // Déclencher l'événement de sélection de date
            if (onDateSelectedHandler != null) {
                ActionEvent event = new ActionEvent(this, dayPane);
                onDateSelectedHandler.handle(event);
            }
        });
    }
    
    /**
     * Définir la date sélectionnée et mettre à jour l'affichage
     */
    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        this.currentYearMonth = YearMonth.from(date);
        populateCalendar(currentYearMonth);
    }
    
    /**
     * Récupérer la date sélectionnée
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }
    
    /**
     * Afficher le mois courant
     */
    public void showCurrentMonth() {
        currentYearMonth = YearMonth.now();
        populateCalendar(currentYearMonth);
    }
    
    /**
     * Définir le gestionnaire d'événements lors de la sélection d'une date
     */
    public void setOnDateSelected(EventHandler<ActionEvent> handler) {
        this.onDateSelectedHandler = handler;
    }
} 