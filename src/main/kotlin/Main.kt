import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class Tiempo(
    val listaObservacionConcellos: List<ListaObservacionConcellos>
)

@Serializable
data class ListaObservacionConcellos(
    val dataLocal: String,
    val dataUTC: String,
    val icoEstadoCeo: Int,
    val icoVento: Int,
    val idConcello: Int,
    val nomeConcello: String,
    val sensacionTermica: Double,
    val temperatura: Double
)

class HTML {
    private val children = mutableListOf<String>()

    fun head(init: HEAD.() -> Unit) {
        val head = HEAD()
        head.init()
        children.add("<head>${head.content}</head>")
    }

    fun body(init: Body.() -> Unit) {
        val body = Body()
        body.init()
        children.add("<body>\n${body.content}</body>")
    }

    override fun toString(): String {
        return "<html>\n" + children.joinToString("\n") + "\n</html>"
    }
}

class HEAD {
    var content: String = ""
    fun title(text: String) {
        content += "<title>$text</title>"
    }
}

class Body {
    var content: String = ""

    fun h1(text: String) {
        content += "\t<h1>$text</h1>\n"
    }

    fun table(text: String) {
        content += "<table>\n$text\n</table>"
    }
}

fun html(init: HTML.() -> Unit): HTML {
    val html = HTML() //crea el objeto receptor
    html.init()// incializamos con la lambda el objeto receptor
    return html
}

fun main() {
    //  crear cliente http
    val client = HttpClient.newHttpClient()

    // crear solicitud
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://servizos.meteogalicia.gal/rss/observacion/observacionConcellos.action"))
        .GET()
        .build()

    //  Enviar la solicitud con el cliente
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    // obtener string con datos
    val jsonBody = response.body()

    // Deserializar el JSON a una lista de objetos clima
    val clima: Tiempo = Json.decodeFromString(jsonBody)

    //Crea El html
    val myHTML = html {
        head {
            title("Clima en Galicia")
        }
        body {
            h1("Clima en Galicia")

            table(
                """
                    <tr>
                        <th>(Concello)</th>
                        <th>(Fecha Local)</th>
                        <th>(Hora UTC)</th>
                        <th>(Temperatura)</th>
                        <th>(Sensación térmica)</th>
                    </tr>
                    
                    ${
                    clima.listaObservacionConcellos.joinToString("\n") { observacion ->
                        """
                    <tr>       
                        <td>${observacion.nomeConcello}</td>
                        <td>${observacion.dataLocal}</td>
                        <td>${observacion.dataUTC}</td>
                        <td>${observacion.temperatura}°C</td>
                        <td>${observacion.sensacionTermica}°C</td>
                     </tr>
                    """.trimIndent()
                    }
                }
                """.trimIndent()
            )
        }
    }
    println(myHTML)
    val file = File("pagina.html")
    file.writeText(myHTML.toString())
}
