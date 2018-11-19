import Utilities.Dialogs;
import Utilities.FileIO;
import Utilities.Print;
import Utilities.VersionData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.Properties;

/**
 * Controller class for MainWindow.fxml, Find.fxml and Settings.fxml
 *
 * Copyright (c) 2018 Tobija Žuntar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Controller extends Component {

    /* MAIN SETTINGS */

    private boolean writeProtected = false;
    private boolean mouseDisabled = false;
    private float opacity = 1f;
    private String dateFormat = "yyyy/MM/dd HH:mm:ss";

    private final String settingsLocation = "Notepad_Settings.xml";

    /**
     * Load settings from an XML file
     */
    void loadSettings() {
        Properties loadSettings = new Properties();

        try {
            loadSettings.loadFromXML(new FileInputStream(settingsLocation));
            writeProtected = Boolean.valueOf(loadSettings.getProperty("write_protected"));
            mouseDisabled = Boolean.valueOf(loadSettings.getProperty("mouse_disabled"));
            opacity = Float.valueOf(loadSettings.getProperty("opacity"));
            dateFormat = loadSettings.getProperty("date_format");
        } catch (IOException e) {
            System.out.println("Loading settings failed, continuing...");
        }

        if (opacity < 0.1f)
            opacity = 0.1f;

        setOtherSettings();
    }

    /**
     * Save settings to an XML file
     */
    void saveSettings() {
        Properties saveSettings = new Properties();
        saveSettings.setProperty("write_protected", String.valueOf(writeProtected));
        saveSettings.setProperty("mouse_disabled", String.valueOf(mouseDisabled));
        saveSettings.setProperty("opacity", String.valueOf(opacity));
        saveSettings.setProperty("date_format", dateFormat);

        try {
            File file = new File(settingsLocation);
            FileOutputStream fileOut = new FileOutputStream(file);
            saveSettings.storeToXML(fileOut, "");
            fileOut.close();
        } catch (IOException e) {
            System.out.println("Saving settings failed, continuing...");
        }
    }

    /*****************************************************************
     *  M A I N       W I N D O W       F X M L       H A N D L E R  *
     *****************************************************************/

    /* Initialize controls */
    public TextArea textEdit = new TextArea();
    public CheckMenuItem wordWrapMenu;
    public Slider opacitySlider = new Slider();
    public MenuBar mainMenuBar = new MenuBar();

    /* FILE OPERATIONS */

    private File file;  // current file
    private boolean modified;   // whether the file has been modified

    /**
     * Create a blank file by deleting the content of textEdit
     */
    public void newFile() {
        boolean confirmedNewFile;

        if (modified) { // if the file has been modified
            confirmedNewFile = Dialogs.confirmationDialog(
                    "Notepad",
                    "Warning",
                    "Changes made to the file since last save will be lost! Continue?");

            if (confirmedNewFile) {     // User selected OK
                textEdit.setText("");
                Main.setTitle("Untitled - Notepad", Main.currentStage);
                modified = false; // the file hasn't been modified yet

                file = null;    // initialize a new file
            }

        } else {    // the file hasn't been modified yet
            textEdit.setText("");
            Main.setTitle("Untitled - Notepad", Main.currentStage);
            modified = false; // the file hasn't been modified yet

            file = null;    // initialize a new file
        }
    }

    /**
     * Display a file chooser dialog and let the user choose a file
     */
    public void openFileDialog() {
        FileChooser fileChooser = new FileChooser(); // Open a file chooser dialog

        fileChooser.getExtensionFilters().addAll(   // set file extensions filter
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));

        file = fileChooser.showOpenDialog(Main.currentStage);

        if (file != null) { // If the user selected a file

            openFile(file);
        }
    }

    /**
     * Open a file and render its content in textEdit
     */
    private void openFile(File file) {
        if (!modified) {    // if the file hasn't been modified yet
            textEdit.setText(FileIO.openFile(file));    // open the file

            // Set the title bar text to match the file's name
            Main.setTitle(file.getName() + " - Notepad", Main.currentStage);
            modified = false; // the file hasn't been modified yet

        } else {    // if the file has been modified
            boolean confirmed;
            confirmed = Dialogs.confirmationDialog( // Ask for confirmation
                    "Notepad",
                    "Warning",
                    "Changes made to the file since last save will be lost! Continue?");

            if (confirmed) {    // user confirmed to discard the changes
                textEdit.setText(FileIO.openFile(file));    // open the file

                // Set the title bar text to match the file's name
                Main.setTitle(file.getName() + " - Notepad", Main.currentStage);
                modified = false; // the file hasn't been modified yet
            }
        }
    }

    /**
     * Save changes to current file or go to saveAs if it's a new file
     */
    public void saveFile() {
        if (file != null) {  // if the text was already saved before

            FileIO.saveFile(file, textEdit.getText());
            Main.setTitle(file.getName() + " - Notepad", Main.currentStage);    // remove the "modified" text
            modified = false; // the file hasn't been modified yet

        } else {    // if this is a new file
            saveAs();
        }
    }

    /**
     * Display a file chooser and let the user select into which file to save the content of textEdit,
     * and then write the text into specified file
     */
    public void saveAs() {
        // Open a file chooser dialog
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().addAll(   // set file extensions filter
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));

        file = fileChooser.showSaveDialog(Main.currentStage);

        if (file != null) { // If the user selected a file
            FileIO.saveFile(file, textEdit.getText());

            // add filename to the title bar
            Main.setTitle(file.getName() + " - Notepad", Main.currentStage);
            modified = false; // the file hasn't been modified yet
        }
    }

    /**
     * Display "(Modified)" text in the title bar when the file was modified
     */
    public void fileModified() {
        if (!modified) {    // if the text isn't already in the title bar
            String currentTitle = Main.currentStage.getTitle();
            Main.setTitle(currentTitle + " (Modified)", Main.currentStage);
            modified = true;
        }
    }

    /* PRINTING */

    /**
     * Call the printing method in Print class
     */
    public void print() {
        Print.printText(textEdit.getText());
    }

    /* OPTIONS MENU FUNCTIONS */

    /**
     * Open a dialog with settings for textEdit
     */
    public void openSettings() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("FXML/Settings.fxml"));
        try {
            Scene scene;
            scene = new Scene(fxmlLoader.load(), 412, 261);
            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (currentFont != null) {
            fontCombo.setPromptText(currentFont);
        }

        dateFormatTextField.setText(dateFormat);
    }

    /**
     * Toggle word wrap according to checked/unchecked state of Word Wrap menu item
     */
    public void toggleWordWrap() {
        if (wordWrapMenu.isSelected()) {  // Word Wrap is checked
            textEdit.setWrapText(true);
        } else {                            // Word Wrap is not checked
            textEdit.setWrapText(false);
        }
    }

    /**
     * Display a dialog and ask the user for a position number and then place the cursor into the specified position
     */
    public void goTo() {
        String defaultValue = Integer.toString(textEdit.getCaretPosition());    // get current position

        TextInputDialog lineNumber = new TextInputDialog(defaultValue);
        lineNumber.setTitle("Notepad");
        lineNumber.setHeaderText("Go to...");
        lineNumber.setContentText("Enter position:");

        Optional<String> result = lineNumber.showAndWait(); // wait for input

        if (result.isPresent()) {
            try {
                textEdit.positionCaret(Integer.parseInt(lineNumber.getResult()));   // place the cursor

            } catch (Exception e) {
                goTo();
            }
        }
    }

    /**
     * Show an About dialog with info about the program
     */
    public void showAboutDialog() {
        String betaNotice = "";

        if (VersionData.isBeta) {
            betaNotice = "BETA Pre-release";
        }

        Dialogs.infoDialog(
                "Notepad",
                "About Notepad",
                "Version: " + VersionData.version +
                        "\nBuild number: " + VersionData.buildNumber + "" +
                        "\nBuild date: " + VersionData.buildDate + "" +
                        "\n" + betaNotice);

    }

    /* EDIT MENU FUNCTIONS */

    /**
     * Undo the last action done in textEdit
     */
    public void undo() {
        textEdit.undo();

        if (textEdit.isUndoable()) {
            fileModified();
        }
    }

    /**
     * Redo the last action done in textEdit
     */
    public void redo() {
        textEdit.redo();

        if (textEdit.isRedoable()) {
            fileModified();
        }
    }

    /**
     * Copy the selected text in textEdit to the system clipboard
     */
    public void copyText() {
        textEdit.copy();
    }

    /**
     * Cut the selected text in textEdit
     */
    public void cutText() {
        textEdit.cut();
        fileModified();
    }

    /**
     * Paste the text from the system clipboard to textEdit
     */
    public void pasteText() {
        textEdit.paste();
        fileModified();
    }

    /**
     * Delete the selected text in textEdit
     */
    public void deleteText() {
        textEdit.replaceSelection("");
        fileModified();
    }

    /**
     * Append the current date and time to textEdit
     */
    public void insertDateTime() {
        String timeStamp = new SimpleDateFormat(dateFormat).format(
                Calendar.getInstance().getTime());

        textEdit.appendText(timeStamp);
        fileModified();
    }

    /**
     * Select all text in textEdit
     */
    public void selectAll() {
        textEdit.selectAll();
    }

    /* OTHER MENU FUNCTIONS */

    public CheckMenuItem disableMouse = new CheckMenuItem();
    public CheckMenuItem menuWriteProtection = new CheckMenuItem();

    /**
     * Change the opacity of MainWindow
     */
    public void changeOpacity() {
        opacity = (float) opacitySlider.getValue() / 100;

        setOtherSettings();
    }

    /**
     * Disable mouse interaction with textEdit
     */
    public void disableMouse() {
        mouseDisabled = disableMouse.isSelected();

        setOtherSettings();
    }

    /**
     * Temporarily write protect textEdit
     */
    public void menuWriteProtection() {
        writeProtected = menuWriteProtection.isSelected();

        setOtherSettings();
    }

    /**
     * Used to apply settings to controls
     */
    private void setOtherSettings() {
        if (writeProtected) {
            textEdit.setEditable(false);
        } else {
            textEdit.setEditable(true);
        }

        if (mouseDisabled) {
            textEdit.setMouseTransparent(true);
            mainMenuBar.setMouseTransparent(true);
        } else {
            textEdit.setMouseTransparent(false);
            mainMenuBar.setMouseTransparent(false);
        }

        if (opacity < 0.01f) {
            opacity = 0.01f;
        }
        Main.currentStage.setOpacity(opacity);

        menuWriteProtection.setSelected(writeProtected);
        disableMouse.setSelected(mouseDisabled);
        opacitySlider.setValue(opacity * 100);
    }

    /**
     * Reset all settings in Other menu
     */
    public void resetOtherSettings() {
        // disable write protection
        writeProtected = false;

        // enable the mouse again
        mouseDisabled = false;

        // reset transparency
        opacity = 1f;

        // reset switches
        opacitySlider.setValue(100);
        menuWriteProtection.setSelected(false);
        disableMouse.setSelected(false);

        setOtherSettings();
    }

    /* CLOSING AND EXITING THE PROGRAM */

    /**
     * Close the program
     */
    public void close() {
        boolean confirmedClose;

        if (modified) { // the file has been modified

            confirmedClose = Dialogs.confirmationDialog(
                    "Notepad",
                    "Warning",
                    "Changes made to the file since last save will be lost! Continue?");

        } else {    // the file hasn't been modified
            confirmedClose = true;
        }
        if (confirmedClose) {     // User selected OK
            saveSettings();
            System.exit(0);
        }
    }


    /*****************************************************************
     *  S E A R C H     W I N D O W      F X M L      H A N D L E R  *
     *****************************************************************/

    /* Initialize controls */
    public TextField searchInput;
    public Button findClose;

    /**
     * Display the search window
     */
    public void openSearch() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("FXML/Find.fxml"));

        try {
            Scene scene;
            scene = new Scene(fxmlLoader.load(), 344, 149);
            Stage stage = new Stage();
            stage.setTitle("Find");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find the specified string in textEdit and select it
     */
    public void find() {    // FIXME: not working
        String searchTerm = searchInput.getText();

        int index = textEdit.getText().indexOf(searchTerm);

        if (index == -1) {  // the text hasn't been found in the file
            Dialogs.compactInfoDialog(
                    "Find",
                    "Could not find " + searchTerm + " in the file");

        } else {    // the text has been found
            // select the text in the file
            textEdit.selectRange(
                    searchTerm.charAt(0),
                    searchTerm.length());
        }
    }

    /**
     * Close the search window
     */
    public void findClose() {
        Stage stage = (Stage) findClose.getScene().getWindow();
        stage.close();
    }


    /*****************************************************************
     *  S E T T I N G S    W I N D O W     F X M L    H A N D L E R  *
     *****************************************************************/

    /* Initialize controls */
    public CheckBox checkboxBold;
    public CheckBox checkboxItalic;
    public TextField fontSize;
    public TextField dateFormatTextField = new TextField();
    public Button btnSettingsClose;
    public ComboBox<String> fontCombo;

    private String currentFont;

    /**
     * Apply font properties to textEdit
     */
    public void settingsApply() {   // FIXME: not working
        dateFormat = dateFormatTextField.getText();

        String selectedFont = fontCombo.getSelectionModel().getSelectedItem();   // Get selected font

        textEdit.setFont(javafx.scene.text.Font.font(selectedFont));
        currentFont = textEdit.getFont().toString();

        if (checkboxBold.isSelected()) {                           // Bold font
            textEdit.setFont(javafx.scene.text.Font.font(
                    currentFont,
                    FontWeight.BOLD,
                    Double.parseDouble(fontSize.getText())));

        } else if (checkboxItalic.isSelected()) {                  // Italic font
            textEdit.setFont(javafx.scene.text.Font.font(
                    currentFont,
                    FontPosture.ITALIC,
                    Double.parseDouble(fontSize.getText())));

        } else if (checkboxBold.isSelected() && checkboxItalic.isSelected()) {    // Bold and italic font
            textEdit.setFont(javafx.scene.text.Font.font(
                    currentFont,
                    FontWeight.BOLD,
                    FontPosture.ITALIC,
                    Double.parseDouble(fontSize.getText())));

        } else {                                                    // Normal font
            textEdit.setFont(new Font(
                    currentFont,
                    Double.parseDouble(fontSize.getText())));
        }
    }

    /**
     * Get the list of installed fonts and set it as the list of items in fontCombo
     */
    public void listFonts() {
        // Get GraphicalEnvironment object
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Get an array list of all fonts
        ObservableList<String> allFonts = FXCollections.observableArrayList(
                ge.getAvailableFontFamilyNames());

        fontCombo.setItems(allFonts);
    }

    /**
     * Reset settings to default values
     */
    public void settingsReset() {
        checkboxBold.setSelected(false);
        checkboxItalic.setSelected(false);
        fontSize.setText("13");
    }

    /**
     * Close the settings window
     */
    public void settingsClose() {
        Stage stage = (Stage) btnSettingsClose.getScene().getWindow();
        stage.close();
    }

} // end class Controller
