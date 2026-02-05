# Vector Service - Spring Boot with Vespa Cloud Integration

A Spring Boot application that provides vector embedding storage and similarity search using Vespa Cloud as the vector database.

## Features

- **POST Endpoint**: Store text, image, or video content with automatic embedding generation
- **GET Endpoint**: Retrieve similar items based on semantic search (returns text by default, media only when requested)
- **Health Check**: Monitor both application and Vespa database health

## Prerequisites

- Java 17+
- Maven 3.8+
- Vespa Cloud instance (or local Vespa deployment)

## Quick Start

### 1. Build the Application

```bash
cd vector-service
mvn clean package -DskipTests
```

### 2. Configure Vespa Connection

Set environment variables or update `application.yml`:

```bash
export VESPA_ENDPOINT=http://localhost:8080
# For Vespa Cloud:
# export VESPA_ENDPOINT=https://your-app.vespa-cloud.com
# export VESPA_CERTIFICATE_PATH=/path/to/cert.pem
# export VESPA_PRIVATE_KEY_PATH=/path/to/key.pem
```

### 3. Deploy Vespa Schema

Deploy the Vespa application from `vespa-app/` directory:

```bash
# For local Vespa
vespa deploy vespa-app

# For Vespa Cloud
vespa deploy vespa-app --target cloud
```

### 4. Run the Application

```bash
mvn spring-boot:run
# Or
java -jar target/vector-service-1.0.0-SNAPSHOT.jar
```

## API Endpoints

### Store Content (POST)

**Store Text:**
```bash
curl -X POST http://localhost:12000/api/v1/vectors/embed \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Lions are majestic animals",
    "contentType": "TEXT",
    "category": "animals"
  }'
```

**Store Image:**
```bash
curl -X POST http://localhost:12000/api/v1/vectors/image \
  -F "file=@image.jpg" \
  -F "description=A photo of a lion" \
  -F "category=animals"
```

**Store Video:**
```bash
curl -X POST http://localhost:12000/api/v1/vectors/video \
  -F "file=@video.mp4" \
  -F "description=Wildlife documentary" \
  -F "category=nature"
```

### Search Similar Content (GET)

**Default (text only):**
```bash
curl "http://localhost:12000/api/v1/vectors/search?query=big cats in africa"
```

**Include media:**
```bash
curl "http://localhost:12000/api/v1/vectors/search?query=big cats&includeMedia=true"
```

**Specific type:**
```bash
curl "http://localhost:12000/api/v1/vectors/search?query=wildlife&preferredType=VIDEO"
```

### Load Sample Data (50 texts)

```bash
curl -X POST http://localhost:12000/api/v1/vectors/load-samples
```

### Health Check

**Full health status:**
```bash
curl http://localhost:12000/api/v1/health
```

**Liveness probe:**
```bash
curl http://localhost:12000/api/v1/health/live
```

**Readiness probe:**
```bash
curl http://localhost:12000/api/v1/health/ready
```

**Database health:**
```bash
curl http://localhost:12000/api/v1/health/db
```

## Project Structure

```
vector-service/
├── src/main/java/com/vectordb/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/             # Data transfer objects
│   ├── model/           # Domain models
│   └── service/         # Business logic
├── src/main/resources/
│   └── application.yml  # Application configuration
├── vespa-app/           # Vespa application config
│   ├── schemas/         # Document schemas
│   ├── services.xml     # Service configuration
│   └── hosts.xml        # Host configuration
└── pom.xml              # Maven dependencies
```

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `vespa.endpoint` | Vespa endpoint URL | `http://localhost:8080` |
| `vespa.tenant` | Vespa tenant name | `default` |
| `vespa.application` | Vespa application name | `vector-app` |
| `embedding.dimension` | Vector dimension size | `384` |
| `server.port` | Application port | `12000` |

## Embedding Model

The application uses a deterministic hash-based embedding approach for demonstration. For production use, integrate with:
- **Text**: Sentence Transformers (all-MiniLM-L6-v2)
- **Images**: CLIP model
- **Videos**: Extract keyframes + CLIP

## Sample Data

The `/api/v1/vectors/load-samples` endpoint loads 50 pre-defined texts:
- 25 texts about animals (lions, elephants, dolphins, etc.)
- 25 texts about cities (New York, Tokyo, Paris, etc.)

## Docker Support

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/vector-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 12000
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t vector-service .
docker run -p 12000:12000 -e VESPA_ENDPOINT=http://vespa:8080 vector-service
```