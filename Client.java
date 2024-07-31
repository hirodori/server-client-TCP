// Importação de pacotes
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {
    public static void main(String[] args) {
        // Nome do Host do Servidor
        String serverHost = "localhost";
        // Porta do Servidor
        int serverPort = 2023;

        // Criação de um Socket para estabelecer conexão com o Servidor e configuração de envio e recepção de dados do Servidor
        try {
            Socket socket = new Socket(serverHost, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            do {
                // Apresentação das opções possíveis a serem tratadas no Servidor
                System.out.println("-------------------");
                System.out.println("Sair\nArquivo\nChat");
                System.out.print("Escolha uma das opções acima para ser tratada no servidor: ");
                String request = userInput.readLine();
                out.println(request);
                System.out.println("-------------------");

                // Opção de Encerrar
                if (request.equals("Sair")) {
                    String response = in.readLine();
                    System.out.println(response);
                    socket.close();
                }
                // Opção de Arquivo
                else if (request.startsWith("Arquivo")) {
                    createFile(in);
                }
                // Opção de Chat
                else if (request.equals("Chat")) {
                    chatHandler(in);
                }
                // Opção inválida
                else {
                    System.out.println("Opção inválida...");
                }
            } while (!socket.isClosed());
        }
        catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void createFile(BufferedReader in) throws IOException, NoSuchAlgorithmException {
        FileWriter fileWriter = new FileWriter("receivedFile");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        String response;
        String status = null, name = null, size = null, hash = null, data;

        // Leitura das informações recebidas
        int i = 0;
        while ((response = in.readLine()) != null && !response.equals("Dados:")) {
            if (response.startsWith("Status: Arquivo inexistente")) {
                break;
            }
            if (i == 0) {
                status = response;
            }
            else if (i == 1) {
                name = response;
            }
            else if (i == 2) {
                size = response;
            }
            else if (i == 3) {
                hash = response;
            }
            i++;
        }
        // Leitura dos dados do arquivo e salvamento deles em um arquivo novo
        data = response;
        while ((response = in.readLine()) != null && !response.equals("Operação encerrada!")) {
            bufferedWriter.write(response + "\n");
            data = data.concat("\n").concat(response);
        }
        bufferedWriter.close();
        fileWriter.close();

        System.out.println("------------------");
        System.out.println("Informações recebidas do arquivo: ");
        System.out.println(status);
        System.out.println(name);
        System.out.println(size);
        System.out.println(hash);
        System.out.println(data);
        System.out.println("------------------");

        assert name != null;
        String[] nameFile = name.split(": ");
        String hashClient = calculatorSHA256(nameFile[1]);
        System.out.println("Hash calculado no Cliente: " + hashClient);

        assert hash != null;
        if (hash.contains(hashClient)) {
            System.out.println("Os hash's foram verificados e são IGUAIS.");
        }
        else {
            System.out.println("Os hash's foram verificados e são DIFERENTES.");
        }

        System.out.println("Novo arquivo de texto criado com sucesso.");
    }

    private static void chatHandler(BufferedReader in) throws IOException {
        String message;
        while ((message = in.readLine()) != null && !message.equals("Sair")) {
            System.out.println("Chat: " + message);
        }
    }

    public static String calculatorSHA256(String fileName) throws IOException, NoSuchAlgorithmException {
        // Classe MessageDigest utilizada para calcular o hash (SHA256)
        MessageDigest md = MessageDigest.getInstance("SHA256");
        // Nome do arquivo para calcular o hash
        FileInputStream fileInputStream = new FileInputStream(fileName);
        // Tamanho máximo do bloco de bytes (1024 bytes)
        byte[] dataBytes = new byte[1024];

        // Loop para ler cada bloco de bytes
        int byteRead;
        while ((byteRead = fileInputStream.read(dataBytes)) != -1) {
            // Atualização do objeto md com os bytes lidos
            md.update(dataBytes, 0, byteRead);
        }

        // Cálculo do hash a partir da função md.digest()
        byte[] hashBytes = md.digest();

        // Conversão para hexadecimal
        StringBuilder hexStringBuilder = new StringBuilder();
        for(byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexStringBuilder.append('0');
            }
            hexStringBuilder.append(hex);
        }

        // Fechamento do arquivo
        fileInputStream.close();
        return hexStringBuilder.toString();
    }
}

