# Money Calculator

Aplicación de escritorio en Java para la conversión de divisas en tiempo real utilizando una API pública de tipos de cambio. El proyecto se ha diseñado aplicando principios SOLID, arquitectura hexagonal (ports & adapters) y patrones de diseño orientados a mejorar mantenibilidad, extensibilidad y testabilidad.

## Descripción general

Money Calculator permite:

- Introducir una cantidad monetaria en una divisa origen.
- Seleccionar una divisa destino.
- Obtener el resultado de la conversión usando el tipo de cambio actual.
- Visualizar el resultado en una interfaz gráfica de usuario basada en Swing.

La aplicación consume un servicio REST de terceros para obtener los tipos de cambio y encapsula el acceso a dicho servicio en una capa de infraestructura desacoplada del dominio.

Se recomienda añadir una captura de pantalla de la interfaz final en la ruta `docs/images/interface.png` y referenciarla con:

```markdown
![Money Calculator Interface](docs/images/interface.png)
```

## Arquitectura y estructura de paquetes

El proyecto está organizado siguiendo una arquitectura hexagonal. El objetivo es separar de forma clara el núcleo de negocio de los detalles tecnológicos (UI, HTTP, librerías externas).

Estructura propuesta:

```text
src/main/java/software/ulpgc/moneycalculator/
├── domain/              # Núcleo del dominio (independiente de frameworks)
│   ├── model/           # Entidades de dominio: Money, Currency, ExchangeRate
│   ├── service/         # Servicios de dominio: CurrencyService, ExchangeRateService
│   └── exception/       # Excepciones específicas del dominio
├── application/         # Casos de uso y puertos (interfaces)
│   ├── usecase/         # Casos de uso: ExchangeMoneyUseCase
│   └── port/            # Puertos: ExchangeRateRepository, CurrencyRepository
├── infrastructure/      # Adaptadores concretos a tecnologías externas
│   ├── api/             # Cliente HTTP: ExchangeRateApiClient
│   │   └── dto/         # DTOs: ExchangeRateResponse, CurrencyListResponse
│   └── config/          # Configuración de la API: ApiConfig
└── presentation/        # Capa de presentación (UI)
    ├── ui/              # Vistas Swing: MoneyCalculatorView, MoneyDisplayPanel
    └── controller/      # Controlador: ExchangeController
```

### Justificación de la estructura

- **Capa domain**: No depende de ninguna librería de infraestructura (ni Swing, ni HTTP, ni Gson). Esto permite reutilizar la lógica de negocio en otros tipos de aplicaciones (por ejemplo, web o CLI) sin cambios.
- **Capa application**: Orquesta casos de uso del dominio a través de puertos (interfaces). Estos puertos son implementados en infrastructure, siguiendo el estilo ports & adapters.
- **Capa infrastructure**: Contiene adaptadores concretos a tecnologías externas, en este caso la API de tipos de cambio y su configuración. Si se cambia de API o se introduce una base de datos, los cambios se localizan aquí.
- **Capa presentation**: Contiene la interfaz de usuario y los controladores que coordinan interacción de usuario y casos de uso.

## Principios SOLID aplicados

### Single Responsibility Principle (SRP)

**Definición**: Cada clase debe tener una única razón de cambio, es decir, una sola responsabilidad.

**Aplicación en el proyecto**:

- **Money, Currency, ExchangeRate**: Estas clases representan conceptos del dominio y encapsulan reglas relacionadas. Por ejemplo, `Money` valida que el importe no sea negativo, maneja la precisión decimal mediante `BigDecimal`, y conoce cómo convertirse a otra divisa usando un tipo de cambio. No tiene responsabilidades adicionales como acceso a datos o presentación.

- **ExchangeRateApiClient**: Su única responsabilidad es comunicarse con la API externa. Se encarga de construir URLs, ejecutar peticiones HTTP, leer la respuesta, parsear JSON en DTOs y mapear esos DTOs a modelos de dominio. No contiene lógica de negocio ni conoce detalles de la interfaz gráfica.

- **ExchangeMoneyUseCase**: Se centra exclusivamente en la lógica del caso de uso "convertir dinero". Coordina la obtención del tipo de cambio desde el repositorio y aplica la conversión, pero no conoce detalles de cómo se obtienen esos datos (HTTP, base de datos, etc.) ni cómo se presentan al usuario.

- **MoneyCalculatorView y MoneyDisplayPanel**: Se ocupan únicamente de la representación gráfica y la captura de entrada del usuario. No contienen lógica de negocio ni acceso a datos.

- **ExchangeController**: Actúa como coordinador entre la vista y los casos de uso. Lee datos de la vista, invoca el caso de uso apropiado y actualiza la vista con el resultado. No implementa lógica de dominio compleja.

**Problema que resuelve**: Evita clases "dios" con múltiples responsabilidades que son difíciles de mantener, testear y reutilizar. Cuando una clase tiene múltiples razones de cambio, los cambios en una responsabilidad pueden afectar inadvertidamente a otras.

### Open/Closed Principle (OCP)

**Definición**: Las entidades de software deben estar abiertas a extensión pero cerradas a modificación.

**Aplicación en el proyecto**:

Se utilizan interfaces para definir contratos que pueden extenderse con nuevas implementaciones sin modificar el código existente:

- **ExchangeRateRepository y CurrencyRepository**: Son interfaces (puertos) que definen operaciones abstractas. `ExchangeRateApiClient` es una implementación concreta, pero podríamos agregar otras implementaciones sin modificar los casos de uso que dependen de estas interfaces.

Ejemplo de extensión:

```java
// Implementación actual
ExchangeRateRepository apiClient = new ExchangeRateApiClient();

// Futura implementación con base de datos (sin modificar ExchangeMoneyUseCase)
ExchangeRateRepository dbClient = new DatabaseExchangeRateRepository();

// Futura implementación con cache
ExchangeRateRepository cachedClient = new CachedExchangeRateRepository(apiClient);
```

**Problema que resuelve**: Reduce el riesgo de introducir errores al agregar nuevas funcionalidades. En lugar de modificar clases existentes (lo que puede romper funcionalidad probada), se crean nuevas clases que implementan las mismas interfaces.

### Liskov Substitution Principle (LSP)

**Definición**: Los objetos de una clase derivada deben poder sustituir objetos de la clase base sin alterar el correcto funcionamiento del programa.

**Aplicación en el proyecto**:

Todas las implementaciones de los puertos deben respetar el contrato definido:

- **ExchangeRateRepository**: Cualquier implementación debe devolver un `ExchangeRate` válido para un par de divisas dado, o lanzar una excepción coherente si no puede obtener el tipo de cambio. No puede devolver `null` inesperadamente ni alterar el comportamiento esperado.

- **CurrencyRepository**: Debe devolver una lista de objetos `Currency` válidos. Las implementaciones no pueden devolver objetos parcialmente inicializados o violar invariantes del dominio.

**Ejemplo**:

```java
public class ExchangeMoneyUseCase {
    private final ExchangeRateRepository repository;

    public Money execute(Money money, Currency targetCurrency) {
        // Este código funciona correctamente independientemente de qué
        // implementación concreta de ExchangeRateRepository se use
        ExchangeRate rate = repository.getExchangeRate(money.currency(), targetCurrency);
        return money.exchange(rate);
    }
}
```

**Problema que resuelve**: Garantiza que las abstracciones sean correctas y que el polimorfismo funcione como se espera. Evita sorpresas al sustituir implementaciones.

### Interface Segregation Principle (ISP)

**Definición**: Los clientes no deberían estar obligados a depender de interfaces que no utilizan.

**Aplicación en el proyecto**:

En lugar de crear una interfaz monolítica `DataRepository` con todos los métodos posibles, se han segregado las responsabilidades:

```java
// INCORRECTO: Interfaz grande y poco cohesiva
interface DataRepository {
    List<Currency> findAllCurrencies();
    Currency findCurrencyByCode(String code);
    ExchangeRate getExchangeRate(Currency from, Currency to);
    void saveTransaction(Transaction t);
    List<Transaction> getTransactionHistory();
}

// CORRECTO: Interfaces segregadas
interface CurrencyRepository {
    List<Currency> findAll();
    Optional<Currency> findByCode(String code);
}

interface ExchangeRateRepository {
    ExchangeRate getExchangeRate(Currency from, Currency to);
}

interface TransactionRepository {
    void save(Transaction t);
    List<Transaction> findAll();
}
```

**Justificación**: `ExchangeMoneyUseCase` solo necesita obtener tipos de cambio, no debería depender de métodos para obtener divisas o guardar transacciones. `CurrencyService` solo necesita acceder a la lista de divisas.

**Problema que resuelve**: Reduce el acoplamiento innecesario y hace que las interfaces sean más cohesivas y fáciles de implementar. Los clientes no se ven forzados a implementar métodos que no necesitan.

### Dependency Inversion Principle (DIP)

**Definición**: Los módulos de alto nivel no deben depender de módulos de bajo nivel. Ambos deben depender de abstracciones.

**Aplicación en el proyecto**:

Los casos de uso (alto nivel) dependen de interfaces abstractas, no de implementaciones concretas:

```java
// ExchangeMoneyUseCase depende de la ABSTRACCIÓN
public class ExchangeMoneyUseCase {
    private final ExchangeRateRepository repository; // Interfaz, no clase concreta

    public ExchangeMoneyUseCase(ExchangeRateRepository repository) {
        this.repository = repository;
    }
}

// La composición se hace en Main (Composition Root)
ExchangeRateApiClient apiClient = new ExchangeRateApiClient(); // Concreto
ExchangeMoneyUseCase useCase = new ExchangeMoneyUseCase(apiClient); // Inyección
```

**Justificación**: El caso de uso no conoce `ExchangeRateApiClient` ni sus detalles de implementación. Solo conoce la interfaz `ExchangeRateRepository`. Esto permite:

- Cambiar la implementación sin modificar el caso de uso.
- Testear el caso de uso inyectando un mock del repositorio.

**Problema que resuelve**: Desacopla los módulos, haciendo el sistema más flexible y testeable. Sin DIP, el caso de uso estaría acoplado a una implementación concreta y sería difícil testearlo sin tocar infraestructura real.

## Patrones de diseño utilizados

### Repository Pattern

**Dónde se aplica**: `ExchangeRateRepository`, `CurrencyRepository`, `ExchangeRateApiClient`.

**Descripción**: El patrón Repository proporciona una abstracción sobre la fuente de datos, encapsulando la lógica de acceso y permitiendo que la capa de aplicación trabaje con objetos del dominio sin preocuparse por cómo se obtienen o persisten.

**Implementación en el proyecto**:

```java
// Interfaz del repositorio (puerto)
public interface ExchangeRateRepository {
    ExchangeRate getExchangeRate(Currency from, Currency to);
}

// Implementación concreta (adaptador)
public class ExchangeRateApiClient implements ExchangeRateRepository {
    @Override
    public ExchangeRate getExchangeRate(Currency from, Currency to) {
        // Lógica HTTP, parseo JSON, mapeo a dominio
    }
}
```

La capa de aplicación (`ExchangeMoneyUseCase`) interactúa con el repositorio como si fuera una colección en memoria, sin conocer detalles de HTTP, JSON o API keys.

**Problema que resuelve**:

- Evita que la lógica de negocio esté acoplada a detalles de acceso a datos.
- Centraliza el código de acceso a datos, facilitando su mantenimiento.
- Permite sustituir la fuente de datos (de API a base de datos, por ejemplo) sin cambiar la lógica de negocio.

### Dependency Injection (DI)

**Dónde se aplica**: Constructores de `ExchangeMoneyUseCase`, `ExchangeController`, `CurrencyService`, `ExchangeRateService`.

**Descripción**: Las dependencias se pasan desde el exterior mediante constructores, en lugar de ser creadas internamente por las clases.

**Implementación en el proyecto**:

```java
// Las dependencias se inyectan por constructor
public class ExchangeMoneyUseCase {
    private final ExchangeRateRepository repository;

    public ExchangeMoneyUseCase(ExchangeRateRepository repository) {
        this.repository = repository;
    }
}

// En Main.java se ensamblan las dependencias
ExchangeRateApiClient apiClient = new ExchangeRateApiClient();
ExchangeMoneyUseCase useCase = new ExchangeMoneyUseCase(apiClient);
```

**Problema que resuelve**:

- Elimina el acoplamiento fuerte a implementaciones concretas.
- Facilita el testing: se pueden inyectar mocks o stubs en lugar de implementaciones reales.
- Mejora la configurabilidad: cambiar implementaciones es tan simple como cambiar qué objeto se inyecta.

### Data Transfer Object (DTO)

**Dónde se aplica**: `ExchangeRateResponse`, `CurrencyListResponse`.

**Descripción**: Los DTOs son objetos simples diseñados para transportar datos entre capas o sistemas. No contienen lógica de negocio.

**Implementación en el proyecto**:

```java
// DTO para la respuesta de la API
public class ExchangeRateResponse {
    @SerializedName("conversion_rate")
    private double conversionRate;

    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("target_code")
    private String targetCode;

    // Getters
}

// Mapeo de DTO a modelo de dominio
ExchangeRateResponse response = gson.fromJson(json, ExchangeRateResponse.class);
ExchangeRate domainObject = new ExchangeRate(
    LocalDate.now(),
    new Currency(response.getBaseCode(), "..."),
    new Currency(response.getTargetCode(), "..."),
    response.getConversionRate()
);
```

**Problema que resuelve**:

- Desacopla el modelo de dominio del contrato de la API externa.
- Permite que el modelo de dominio evolucione independientemente del formato JSON.
- Centraliza la lógica de parseo y validación de datos externos.

### Use Case Pattern

**Dónde se aplica**: `ExchangeMoneyUseCase`.

**Descripción**: Encapsula la lógica de un caso de uso específico de la aplicación. Cada caso de uso representa una operación que el usuario puede realizar.

**Implementación en el proyecto**:

```java
public class ExchangeMoneyUseCase {
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeMoneyUseCase(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public Money execute(Money money, Currency targetCurrency) {
        ExchangeRate rate = exchangeRateRepository.getExchangeRate(
            money.currency(), 
            targetCurrency
        );
        return money.exchange(rate);
    }
}
```

**Flujo**:
1. Recibe el dinero origen y la divisa destino.
2. Obtiene el tipo de cambio del repositorio.
3. Aplica la conversión usando el modelo de dominio.
4. Devuelve el resultado.

**Problema que resuelve**:

- Evita mezclar lógica de negocio con código de presentación o infraestructura.
- Facilita la reutilización: el mismo caso de uso puede invocarse desde diferentes interfaces (UI Swing, API REST, CLI).
- Mejora la testabilidad: los casos de uso se pueden probar aisladamente.

### MVC (Model-View-Controller)

**Dónde se aplica**: Capa de presentación.

**Componentes**:

- **Model**: `Money`, `Currency`, `ExchangeRate` (compartidos desde el dominio).
- **View**: `MoneyCalculatorView`, `MoneyDisplayPanel` (componentes Swing).
- **Controller**: `ExchangeController`.

**Flujo de interacción**:

1. El usuario introduce datos en la vista (cantidad, divisas).
2. La vista notifica al controlador cuando se presiona el botón de conversión.
3. El controlador lee los datos de la vista, invoca el caso de uso y actualiza la vista con el resultado.

```java
public class ExchangeController {
    private final ExchangeMoneyUseCase exchangeMoneyUseCase;

    public void handleExchange() {
        Money inputMoney = view.getInputMoney();
        Currency targetCurrency = view.getTargetCurrency();
        Money result = exchangeMoneyUseCase.execute(inputMoney, targetCurrency);
        view.displayResult(result);
    }
}
```

**Problema que resuelve**:

- Separa la lógica de presentación de la lógica de negocio.
- Permite cambiar la interfaz gráfica sin afectar el dominio.
- Facilita el testing de la lógica del controlador y de la vista por separado.

### Value Object Pattern

**Dónde se aplica**: `Money`, `Currency`, `ExchangeRate`.

**Descripción**: Los Value Objects son objetos inmutables que se identifican por su valor, no por su identidad. Encapsulan reglas de validación y comportamiento relacionado con ese valor.

**Implementación en el proyecto**:

```java
public class Money {
    private final BigDecimal amount;  // Inmutable
    private final Currency currency;  // Inmutable

    public Money(BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    // No hay setters - inmutabilidad garantizada

    public Money exchange(ExchangeRate rate) {
        BigDecimal convertedAmount = amount.multiply(BigDecimal.valueOf(rate.rate()));
        return new Money(convertedAmount, rate.to());
    }
}
```

**Características**:

- **Inmutabilidad**: No exponen setters, las operaciones devuelven nuevos objetos.
- **Validación**: Los invariantes se verifican en el constructor.
- **Comportamiento**: Encapsulan operaciones relacionadas (por ejemplo, `exchange`).

**Problema que resuelve**:

- Previene bugs causados por mutabilidad inesperada.
- Garantiza thread-safety sin sincronización adicional.
- Mejora la expresividad del dominio: `Money` es más claro que un `double` suelto.

### Factory / Composition Root

**Dónde se aplica**: Clase `Main`.

**Descripción**: La clase `Main` actúa como punto de ensamblaje de toda la aplicación, creando e inyectando todas las dependencias.

**Implementación en el proyecto**:

```java
public class Main {
    public static void main(String[] args) {
        // Crear adaptadores de infraestructura
        ExchangeRateApiClient apiClient = new ExchangeRateApiClient();

        // Crear servicios
        CurrencyService currencyService = new CurrencyService(apiClient);

        // Crear casos de uso
        ExchangeMoneyUseCase exchangeMoneyUseCase = new ExchangeMoneyUseCase(apiClient);

        // Crear controladores
        ExchangeController controller = new ExchangeController(exchangeMoneyUseCase);

        // Crear vista
        MoneyCalculatorView view = new MoneyCalculatorView();
        controller.setView(view);

        // Inicializar
        initializeApplication(view, controller, currencyService);
    }
}
```

**Problema que resuelve**:

- Centraliza la configuración de dependencias en un único lugar.
- Hace explícitas todas las relaciones entre componentes.
- Facilita cambios de configuración sin tocar el código de negocio.

## Tecnologías utilizadas

- **Java 21**: Lenguaje de programación principal.
- **Swing**: Framework para la interfaz gráfica de usuario.
- **Gson 2.11.0**: Librería para parseo de JSON.
- **JUnit 5.11.4**: Framework de testing unitario.
- **Mockito 5.14.2**: Framework para creación de mocks en tests.
- **Maven**: Herramienta de construcción y gestión de dependencias.

## Interfaz de usuario

La interfaz final implementada con Swing incluye:

- **Campo de texto numérico**: Para introducir la cantidad a convertir.
- **ComboBox de divisa origen**: Lista desplegable con todas las divisas disponibles.
- **ComboBox de divisa destino**: Lista desplegable para seleccionar la divisa objetivo.
- **Botón Exchange**: Ejecuta la conversión al hacer clic.
- **Panel de resultado**: Muestra el importe convertido con formato y la información de la divisa.
- **Manejo de errores**: Diálogos de error para entradas inválidas o fallos de red.

Se recomienda añadir una captura de pantalla de la aplicación en ejecución en `docs/images/interface.png`.

## Ejecución del proyecto

### Requisitos previos

- JDK 21 o superior
- Maven 3.6 o superior

### Compilación

```bash
mvn clean compile
```

### Ejecución desde Maven

```bash
mvn exec:java -Dexec.mainClass="software.ulpgc.moneycalculator.Main"
```

### Ejecución desde IntelliJ IDEA

1. Abrir el proyecto en IntelliJ
2. Navegar a `src/main/java/software/ulpgc/moneycalculator/Main.java`
3. Clic derecho y seleccionar "Run 'Main.main()'"

### Generar JAR ejecutable

```bash
mvn package
java -jar target/moneycalculator-2.0.0.jar
```

### Ejecutar tests

```bash
mvn test
```

## Testing

El proyecto incluye tests unitarios organizados por capas:

### Tests de dominio

Verifican la lógica de negocio encapsulada en los modelos:

```java
@Test
void shouldCreateMoneyWithValidAmount() {
    Currency usd = new Currency("USD", "US Dollar");
    Money money = new Money(100.0, usd);

    assertEquals(BigDecimal.valueOf(100.00).setScale(2), money.amount());
    assertEquals(usd, money.currency());
}

@Test
void shouldExchangeMoney() {
    Currency usd = new Currency("USD", "US Dollar");
    Currency eur = new Currency("EUR", "Euro");
    Money money = new Money(100.0, usd);

    ExchangeRate rate = new ExchangeRate(LocalDate.now(), usd, eur, 0.85);
    Money result = money.exchange(rate);

    assertEquals(85.0, result.amount().doubleValue(), 0.01);
    assertEquals(eur, result.currency());
}
```

### Tests de casos de uso

Verifican la lógica de aplicación usando mocks:

```java
@Test
void shouldExchangeMoney() {
    Currency usd = new Currency("USD", "US Dollar");
    Currency eur = new Currency("EUR", "Euro");
    Money money = new Money(100.0, usd);
    ExchangeRate rate = new ExchangeRate(LocalDate.now(), usd, eur, 0.85);

    ExchangeRateRepository mockRepo = mock(ExchangeRateRepository.class);
    when(mockRepo.getExchangeRate(usd, eur)).thenReturn(rate);

    ExchangeMoneyUseCase useCase = new ExchangeMoneyUseCase(mockRepo);
    Money result = useCase.execute(money, eur);

    assertEquals(85.0, result.amount().doubleValue(), 0.01);
    verify(mockRepo, times(1)).getExchangeRate(usd, eur);
}
```

### Tests de integración

Verifican la comunicación real con la API (opcional, requiere conectividad):

```java
@Test
void shouldLoadCurrenciesFromApi() {
    ExchangeRateApiClient client = new ExchangeRateApiClient();
    List<Currency> currencies = client.findAll();

    assertNotNull(currencies);
    assertFalse(currencies.isEmpty());
    assertTrue(currencies.stream().anyMatch(c -> c.code().equals("USD")));
}
```

## Ventajas de la arquitectura implementada

**Testabilidad**: La lógica de negocio está completamente aislada de la infraestructura. Los tests unitarios pueden ejecutarse sin tocar la API real ni la base de datos, inyectando mocks.

**Mantenibilidad**: Los cambios están localizados. Si cambia el formato de la API, solo se modifica `ExchangeRateApiClient` y los DTOs. Si cambia la UI, solo se toca la capa de presentación.

**Escalabilidad**: Es sencillo añadir nuevas funcionalidades:
- Nuevas fuentes de datos (implementar los puertos).
- Nuevas interfaces de usuario (reutilizar casos de uso).
- Nuevas reglas de negocio (agregar servicios de dominio).

**Independencia tecnológica**: El dominio no conoce Swing, Gson ni HTTP. Puede portarse a otras plataformas (web, móvil) reutilizando toda la lógica de negocio.

**Cumplimiento de SOLID**: Cada principio se aplica conscientemente, resultando en un diseño robusto y flexible.

## Posibles mejoras futuras

- **Cache persistente**: Implementar un `CachedExchangeRateRepository` usando Redis o una base de datos local para reducir llamadas a la API.
- **Histórico de conversiones**: Agregar un `TransactionRepository` para guardar y consultar conversiones previas.
- **Múltiples APIs con fallback**: Implementar un patrón Chain of Responsibility para intentar varias fuentes de datos si una falla.
- **Interfaz web**: Crear una API REST con Spring Boot reutilizando los casos de uso existentes.
- **Gráficos de tendencias**: Visualizar la evolución histórica de tipos de cambio.
- **Modo offline**: Guardar los últimos tipos de cambio conocidos para trabajar sin conexión.
- **Configuración externalizada**: Mover la API key a un archivo de configuración o variables de entorno.

## Autor

Proyecto desarrollado por Asmae Ez zaim Driouch como práctica de la asignatura Ingeniería del Software 2.  
Universidad de Las Palmas de Gran Canaria (ULPGC).
