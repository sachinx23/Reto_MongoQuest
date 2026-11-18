package controlador;

/**
 * Clase de utilidad encargada de validar los datos personales, de contacto y bancarios
 * de los perfiles de usuario (principalmente clientes) dentro de la aplicación.
 * Proporciona métodos estáticos para verificar el formato y dígito de control de
 * documentos de identidad (DNI/NIE), correos electrónicos, teléfonos, códigos postales
 * y cuentas bancarias IBAN españolas.
 *
 * @author Eneko.Plaza
 */
public class ValidarPerfil {

    /**
     * Cadena de referencia con las letras de control ordenadas según el resto de dividir
     * el número del DNI o NIE entre 23.
     */
    private static final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";

    /**
     * Comprueba si un Documento Nacional de Identidad (DNI) o Número de Identidad de Extranjero (NIE)
     * tiene una longitud de 9 caracteres, un formato numérico válido (sustituyendo las letras iniciales
     * {@code X}, {@code Y}, {@code Z} por {@code 0}, {@code 1}, {@code 2} respectivamente en el caso del NIE)
     * y una letra final coincidente con el algoritmo oficial módulo 23.
     *
     * @param documento Cadena con el DNI o NIE a validar.
     * @return {@code true} si el documento cumple el formato y su letra de control es correcta;
     *         {@code false} en caso contrario.
     */
    public static boolean dniNieValido(String documento) {
        documento = documento.trim().toUpperCase();
        if (documento.length() != 9) {
            return false;
        }

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
     * Valida que una dirección de correo electrónico presente una estructura básica válida
     * ({@code usuario@dominio.extension}), sin espacios y con un dominio de al menos dos letras.
     *
     * @param correo Cadena con la dirección de correo electrónico a comprobar.
     * @return {@code true} si el formato del correo electrónico es válido; {@code false} en caso contrario.
     */
    public static boolean correoValido(String correo) {
        return correo.trim().matches("[^@ ]+@[^@ ]+\\.[a-zA-Z]{2,}");
    }

    /**
     * Valida que un número de teléfono esté compuesto exactamente por 9 dígitos numéricos,
     * ignorando los espacios en blanco intermedios.
     *
     * @param telefono Cadena con el número de teléfono a comprobar.
     * @return {@code true} si contiene exactamente 9 dígitos; {@code false} en caso contrario.
     */
    public static boolean telefonoValido(String telefono) {
        return telefono.replace(" ", "").matches("[0-9]{9}");
    }

    /**
     * Valida que un código postal español esté formado exactamente por 5 dígitos numéricos.
     *
     * @param codigoPostal Cadena con el código postal a comprobar.
     * @return {@code true} si está compuesto por 5 dígitos; {@code false} en caso contrario.
     */
    public static boolean codigoPostalValido(String codigoPostal) {
        return codigoPostal.trim().matches("[0-9]{5}");
    }

    /**
     * Valida el formato de un código de cuenta bancaria IBAN español ({@code ES} seguido de 22 dígitos),
     * ignorando espacios en blanco y diferencias entre mayúsculas y minúsculas. Permite también
     * que el campo se deje vacío.
     *
     * @param cuenta Cadena con el número de cuenta bancaria IBAN a comprobar.
     * @return {@code true} si la cadena está vacía o cumple el patrón {@code ES[0-9]{22}};
     *         {@code false} en caso contrario.
     */
    public static boolean cuentaValida(String cuenta) {
        String sinEspacios = cuenta.replace(" ", "").toUpperCase();
        return sinEspacios.isEmpty() || sinEspacios.matches("ES[0-9]{22}");
    }

    /**
     * Ejecuta una validación integral de todos los campos del perfil de un cliente,
     * verificando que los datos obligatorios no estén vacíos y cumplan con sus respectivos
     * formatos (DNI/NIE, correo electrónico, teléfono y cuenta bancaria opcional).
     *
     * @param nombre        Nombre de pila del cliente.
     * @param nombreUsuario Nombre de usuario de acceso.
     * @param dni           Documento de identidad (DNI o NIE) del cliente.
     * @param correo        Dirección de correo electrónico.
     * @param telefono      Número de teléfono de contacto.
     * @param direccion     Dirección postal del cliente.
     * @param cuenta        Número de cuenta bancaria IBAN (puede estar vacío).
     * @return Un mensaje descriptivo con el primer error de validación detectado,
     *         o {@code null} si todos los datos son correctos.
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