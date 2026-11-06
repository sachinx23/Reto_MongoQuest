package controlador;

/**
 * Comprobaciones de los datos que se escriben en los formularios.
 */
public class ValidarPerfil {

    // Letras del DNI: la letra correcta es la de la posición (número % 23)
    private static final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";

    /**
     * Comprueba un DNI (12345678Z) o un NIE (X1234567L), incluida la letra final.
     *
     * @param documento DNI o NIE
     * @return true si el formato y la letra son correctos
     */
    public static boolean dniNieValido(String documento) {
        documento = documento.trim().toUpperCase();
        if (documento.length() != 9) {
            return false;
        }

        // En el NIE la primera letra se cambia por un número: X = 0, Y = 1, Z = 2
        String numeros = documento.substring(0, 8);
        char primera = documento.charAt(0);
        if (primera == 'X') {
            numeros = "0" + documento.substring(1, 8);
        } else if (primera == 'Y') {
            numeros = "1" + documento.substring(1, 8);
        } else if (primera == 'Z') {
            numeros = "2" + documento.substring(1, 8);
        }

        if (!numeros.matches("[0-9]{8}")) {
            return false;
        }
        int resto = Integer.parseInt(numeros) % 23;
        char letraCorrecta = LETRAS_DNI.charAt(resto);
        return documento.charAt(8) == letraCorrecta;
    }

    /**
     * Texto@texto.texto (al menos 2 letras al final, como .com o .es).
     *
     * @param correo correo electrónico
     * @return true si tiene el formato texto@texto.dominio
     */
    public static boolean correoValido(String correo) {
        return correo.trim().matches("[^@ ]+@[^@ ]+\\.[a-zA-Z]{2,}");
    }

    /**
     * 9 números; se permiten espacios entre ellos (666 666 666).
     *
     * @param telefono número de teléfono
     * @return true si tiene 9 números (se ignoran los espacios)
     */
    public static boolean telefonoValido(String telefono) {
        return telefono.replace(" ", "").matches("[0-9]{9}");
    }

    /**
     * Vacía (es opcional) o IBAN español: ES + 22 números; se permiten espacios Código postal: 5 números.
     *
     * @param codigoPostal código postal
     * @return true si tiene 5 números
     */
    public static boolean codigoPostalValido(String codigoPostal) {
        return codigoPostal.trim().matches("[0-9]{5}");
    }

    /**
     * Comprueba la cuenta bancaria: ES seguido de 22 números (vacía también vale, porque es opcional).
     *
     * @param cuenta cuenta bancaria (IBAN)
     * @return true si tiene el formato correcto o está vacía
     */
    public static boolean cuentaValida(String cuenta) {
        String sinEspacios = cuenta.replace(" ", "").toUpperCase();
        return sinEspacios.isEmpty() || sinEspacios.matches("ES[0-9]{22}");
    }

    /**
     * Datos de un cliente (registro y Mi Perfil). Devuelve el mensaje de error, o null si todo es correcto.
     *
     * @param nombre nombre
     * @param nombreUsuario nombre de usuario
     * @param dni DNI o NIE
     * @param correo correo electrónico
     * @param telefono número de teléfono
     * @param direccion dirección
     * @param cuenta cuenta bancaria (IBAN)
     * @return el mensaje del primer error, o null si todo está bien
     */
    public static String comprobarCliente(String nombre, String nombreUsuario, String dni, String correo,
            String telefono, String direccion, String cuenta) {
        if (nombre.trim().isEmpty() || nombreUsuario.trim().isEmpty() || dni.trim().isEmpty()
                || correo.trim().isEmpty() || telefono.trim().isEmpty() || direccion.trim().isEmpty()) {
            return "Por favor, rellena todos los campos.";
        }
        if (!dniNieValido(dni)) {
            return "DNI o NIE incorrecto (comprueba también la letra).";
        }
        if (!correoValido(correo)) {
            return "El correo no es válido.";
        }
        if (!telefonoValido(telefono)) {
            return "El teléfono debe tener 9 números.";
        }
        if (!cuentaValida(cuenta)) {
            return "La cuenta bancaria debe ser ES seguido de 22 números (o dejarla vacía).";
        }
        return null;
    }
}
