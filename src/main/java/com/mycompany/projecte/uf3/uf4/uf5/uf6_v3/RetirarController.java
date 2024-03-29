package com.mycompany.projecte.uf3.uf4.uf5.uf6_v3;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RetirarController {

    @FXML
    
    private void switchToMenu() throws IOException {
        App.setRoot("menu");
    }
    
    @FXML
    ChoiceBox<String> compteEscollit;

    @FXML
    TextField quantitatRetirar;
    
    @FXML
    TextField bill5;
    
    @FXML
    TextField bill10;
    
    @FXML
    TextField bill20;    
        
    @FXML
    TextField bill50;
    
    @FXML
    TextField bill100;
    
    @FXML
    TextField bill200;
    
    @FXML
    TextField bill500;

    @FXML
    Label error;
            
    ConnectionClass connectionClass = new ConnectionClass();
    Connection connection = connectionClass.getConnection();
    
    /**
    * Aquest mètode s'anomena quan es carrega la vista.
    * Recupera els comptes de l'usuari que ha iniciat sessió de la base de dades i n'omple el ChoiceBox.
    * @throws SQLException si hi ha un error en executar la consulta SQL
    */
    @FXML
    public void initialize() throws SQLException {
        String selectSql = "SELECT * FROM compte WHERE usuari_id = "+AlmacenarUsuario.usuari;
        Statement selectStatement = connection.createStatement();
        ResultSet resultSet = selectStatement.executeQuery(selectSql);

        List<String> opciones = new ArrayList<>();

        while (resultSet.next()) {
            opciones.add("Compte "+resultSet.getString("id")+": "+resultSet.getString("saldo"));
            // reemplaza 'nombre_de_la_columna' por el nombre real de la columna
            // que contiene los valores que deseas mostrar en el ChoiceBox
        }

        ObservableList<String> opcionesObservable = FXCollections.observableArrayList(opciones);
        compteEscollit.setItems(opcionesObservable);
    }

    /**
     * Aquest mètode s'anomena quan l'usuari fa clic al botó "Retirar". Retira
     * la quantitat especificada de diners del compte seleccionat i actualitza
     * el seu saldo a la base de dades. També registra la retirada a la taula
     * "moviment" i actualitza l'inventari de bitllets a la taula
     * "inventari_billets".
     *
     * @throws SQLException si hi ha un error en executar la consulta SQL
     */
    public void retirarDiners() throws SQLException {
        
        String opcioSeleccionada = compteEscollit.getValue();
        
        // Utiliza una expresión regular para encontrar el valor numérico entre "Compte " y ":"
        String regex = "Compte\\s(\\d+):\\s\\d+\\.\\d+"; // Patrón regular
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(opcioSeleccionada);
        
        int valorInt = 0;
        // Verifica si se encuentra el patrón y obtén el valor de la "X"
        if (matcher.find()) {
            String valorX = matcher.group(1);
            valorInt = Integer.parseInt(valorX);
            System.out.println("Valor de X: " + valorInt);
            // Realiza las acciones necesarias con el valor de X
            registraMoviment(valorInt);
        } else {
            System.out.println("No se encontró el valor de X");
        }
        
        String textFieldContent = quantitatRetirar.getText();
        double valorDouble = 0;
        try {
            valorDouble = Double.parseDouble(textFieldContent);
            System.out.println("El contenido del TextField es un número double: " + valorDouble);
            // Realiza las acciones necesarias con el valor double
        } catch (NumberFormatException e) {
            System.out.println("El contenido del TextField no es un número double válido");
            // Realiza las acciones necesarias en caso de excepción
            valorDouble = 0;
        }
        
        String updateSql = "UPDATE compte SET saldo = saldo - "+valorDouble+" WHERE id = "+valorInt;
        Statement updateStatement = connection.prepareStatement(updateSql);
        updateStatement.executeUpdate(updateSql);
        initialize();
        error.setText("S'han retirat diners");
    }
    
    /**
    * Registra un moviment de retirada d'efectiu a la base de dades amb la informació
    * proporcionada per l'usuari.
    * 
    * @param compteId Identificador del compte del qual es retira l'efectiu.
    * @throws SQLException Si hi ha algun problema amb l'accés a la base de dades.
    */
    public void registraMoviment(int compteId) throws SQLException {
        // Obtener la fecha actual
        LocalDate fechaActual = LocalDate.now();

        // Crear un formateador de fecha con el patrón "yyyy-MM-dd"
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Formatear la fecha actual usando el formateador
        String fechaFormateada = fechaActual.format(formateador);

        String selectSql = "INSERT INTO moviment (tipusDeMoviment, data, quantitat, compteOrigen_id) \n" +
            "VALUES ('Retirar', '"+fechaFormateada+"', "+Double.parseDouble(quantitatRetirar.getText())+", "+compteId+");";

        PreparedStatement stmt = connection.prepareStatement(selectSql);
        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("El registro se insertó correctamente.");
            actualitzarInventari();
        } else {
            System.out.println("El registro no se insertó correctamente.");
        }
    }
    
    /**
    * Calcula la quantitat total d'efectiu que es retirarà basant-se en el nombre
    * de bitllets de cada denominació que s'han introduït per l'usuari.
    */
    public void calcularDiners () {
        try {
            int quantBitllets5 = Integer.parseInt(bill5.getText().isEmpty() ? "0" : bill5.getText());
            int quantBitllets10 = Integer.parseInt(bill10.getText().isEmpty() ? "0" : bill10.getText());
            int quantBitllets20 = Integer.parseInt(bill20.getText().isEmpty() ? "0" : bill20.getText());
            int quantBitllets50 = Integer.parseInt(bill50.getText().isEmpty() ? "0" : bill50.getText());
            int quantBitllets100 = Integer.parseInt(bill100.getText().isEmpty() ? "0" : bill100.getText());
            int quantBitllets200 = Integer.parseInt(bill200.getText().isEmpty() ? "0" : bill200.getText());
            int quantBitllets500 = Integer.parseInt(bill500.getText().isEmpty() ? "0" : bill500.getText());


            int total = (5 * quantBitllets5) + 
                        (10 * quantBitllets10) +
                        (20 * quantBitllets20) +
                        (50 * quantBitllets50) +
                        (100 * quantBitllets100) +
                        (200 * quantBitllets200) +
                        (500 * quantBitllets500);

            quantitatRetirar.setText(String.valueOf(total));
        } catch (NumberFormatException e) {
            // Manejar la excepción
            System.out.println("Error: alguno de los campos de texto no contiene un número válido.");
        }
    }
    
    /**
    * Actualitza el registre de l'inventari de bitllets amb les quantitats corresponents
    * a les denominacions de bitllets que s'han introduït per l'usuari.
    * 
    * @throws SQLException Si hi ha algun problema amb l'accés
    */
public void actualitzarInventari() throws SQLException {
    String updateSql = "UPDATE inventari_billets " +
            "SET quantitat = CASE valor_bitllet " +
            "WHEN 5 THEN quantitat - ? " +
            "WHEN 10 THEN quantitat - ? " +
            "WHEN 20 THEN quantitat - ? " +
            "WHEN 50 THEN quantitat - ? " +
            "WHEN 100 THEN quantitat - ? " +
            "WHEN 200 THEN quantitat - ? " +
            "WHEN 500 THEN quantitat - ? " +
            "END " +
            "WHERE valor_bitllet IN (5, 10, 20, 50, 100, 200, 500)";

    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
        // Establecer los nuevos valores y validar antes de convertirlos a enteros
        updateStatement.setInt(1, parseAndValidateInteger(bill5.getText()));
        updateStatement.setInt(2, parseAndValidateInteger(bill10.getText()));
        updateStatement.setInt(3, parseAndValidateInteger(bill20.getText()));
        updateStatement.setInt(4, parseAndValidateInteger(bill50.getText()));
        updateStatement.setInt(5, parseAndValidateInteger(bill100.getText()));
        updateStatement.setInt(6, parseAndValidateInteger(bill200.getText()));
        updateStatement.setInt(7, parseAndValidateInteger(bill500.getText()));

        int rowsAffected = updateStatement.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("S'han actualitzat " + rowsAffected + " files.");
        } else {
            System.out.println("No s'han actualitzat files.");
        }
    }
}

private int parseAndValidateInteger(String value) {
    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException e) {
        System.out.println("El valor '" + value + "' no es un número entero válido");
        return 0;
    }
}

}