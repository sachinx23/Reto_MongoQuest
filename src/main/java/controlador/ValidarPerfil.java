package controlador;

public class ValidarPerfil {
    // Letras del DNI
    private static final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";

    public static boolean dniNieValido(String documento) {
        documento = documento.trim().toUpperCase();
        if (documento.length() != 9) {
            return false;
        }

        // NIE: X=0, Y=1, Z=2
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

    public static boolean correoValido(String correo) {
        return correo.trim().matches("[^@ ]+@[^@ ]+\\.[a-zA-Z]{2,}");
    }

    public static boolean telefonoValido(String telefono) {
        return telefono.replace(" ", "").matches("[0-9]{9}");
    }

    public static boolean codigoPostalValido(String codigoPostal) {
        return codigoPostal.trim().matches("[0-9]{5}");
    }

    public static boolean cuentaValida(String cuenta) {
        String sinEspacios = cuenta.replace(" ", "").toUpperCase();
        return sinEspacios.isEmpty() || sinEspacios.matches("ES[0-9]{22}");
    }

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
