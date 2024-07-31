// Importação de pacotes
import java.io.*;
import java.net.*;
import java.security.*;
import java.nio.file.*;
import java.util.Objects;

public class ServerWeb {
    public static void main(String[] args) {
        // Criação do Socket do Servidor para a porta 2023
        try (ServerSocket serverSocket = new ServerSocket(2023)) {

            // Mensagem indicando que o Servidor está esperando por conexões de clientes
            System.out.println("Servidor está aguardando por conexões...");

            // Loop infinito para que o Servidor possa continuar aceitando conexões indefinidamente
            while (true) {
                // Chamada que bloqueia o Servidor até que uma conexão seja estabelecida (Retorna um "Socket do Cliente")
                Socket clientSocket = serverSocket.accept();
                // Mensagem que mostra o endereço IP do Cliente conectado
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                // Criação de uma thread a partir de uma instância do tipo ClientHandler para lidar com várias conexões de clientes concorrentemente
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Classe para tratar as conexões dos clientes em threads separadas
class ClientHandler implements Runnable {
    // Variável que representa o Socket do cliente atual
    private final Socket clientSocket;

    // Construtor recebe o Socket do Cliente e o armazena na variável da classe
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Configuração para enviar e receber dados do Cliente através do Socket do Cliente
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            do {
                String request;
                // Lê a primeira linha recebida do Cliente
                request = in.readLine();

                // Encerrar a conexão
                if (request.equals("Sair")) {
                    out.println("Servidor: Conexão encerrada!");
                    System.out.println("Cliente encerrado: " + clientSocket.getInetAddress());
                    clientSocket.close();
                }
                // Requisições sobre um arquivo
                else if (request.startsWith("Arquivo")) {
                    String fileName = request.split(" ")[1];
                    sendFile(out, fileName);
                }
                // Chat
                else if (request.equals("Chat")) {
                    chatHandler(out);
                }
            } while (!clientSocket.isClosed());
        }
        catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(PrintWriter out, String fileName) throws IOException, NoSuchAlgorithmException {
        File file = new File(fileName);

        // Verifica se o arquivo existe
        if (!file.exists()) {
            out.println("Status: Arquivo inexistente");
        }
        else {
            // Faz a leitura "bruta" (array de bytes) do arquivo
            byte[] data = Files.readAllBytes(file.toPath());
            // Calcula o hash SHA-256 do arquivo
            String hash = calculatorSHA256(fileName);

            FileReader fileAux = new FileReader(fileName);
            BufferedReader reader = new BufferedReader(fileAux);
            String row;

            // Envio das informações do arquivo
            out.println("Status: Arquivo existente");
            out.println("Nome do arquivo: " + fileName);
            out.println("Tamanho: " + data.length);
            out.println("Hash: " + hash);
            out.println("Dados:");
            // Leitura de cada linha de dados do arquivo
            while ((row = reader.readLine()) != null) {
                out.println(row);
            }
        }
        out.println("Operação encerrada!");
    }

    private static void chatHandler(PrintWriter out) throws IOException {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        String message;
        do {
            message = userInput.readLine();
            out.println(message);
        } while (!Objects.equals(message, "Sair"));
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

