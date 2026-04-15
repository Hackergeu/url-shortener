# ✂️ URL Shortener with Analytics

A full-stack URL shortening service built with Java Spring Boot and MySQL.
Shorten long URLs, track clicks, generate QR codes, and view analytics — all in one app.

## 🌐 Live Demo
> Coming soon after deployment

## 📸 Features
- 🔗 Shorten any long URL instantly
- ✏️ Custom alias support (e.g. `/my-link`)
- ⏳ Link expiry (set expiry in days)
- 📊 Real-time click analytics dashboard
- 📱 QR code generation for every short URL
- 🖥️ Device tracking (Mobile vs Desktop)
- 🗑️ Delete URLs from dashboard
- ⚡ Multithreaded click logging (non-blocking)

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot |
| Database | MySQL, Spring Data JPA |
| Frontend | HTML, CSS, Bootstrap 5, JavaScript |
| QR Code | ZXing (Google) |
| Build Tool | Maven |

## 🏗️ Project Structure
```
src/
├── main/
│   ├── java/com/urlshortener/
│   │   ├── controller/     → REST API endpoints
│   │   ├── model/          → Database entities
│   │   ├── repository/     → Database queries
│   │   └── service/        → Business logic
│   └── resources/
│       └── static/         → Frontend HTML files
```

## 🚀 Run Locally

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3+

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/Hackergeu/url-shortener.git
cd url-shortener
```

**2. Create MySQL database**
```sql
CREATE DATABASE urlshortener;
```

**3. Configure database**

Create `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/urlshortener
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
server.port=8080
server.address=0.0.0.0
```

**4. Run the app**
```bash
mvn spring-boot:run
```

**5. Open in browser**
```
http://localhost:8080/index.html
```

## 📡 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/shorten` | Shorten a URL |
| GET | `/api/r/{shortCode}` | Redirect to original URL |
| GET | `/api/analytics/{shortCode}` | Get click analytics |
| GET | `/api/urls` | Get all shortened URLs |
| GET | `/api/qr/{shortCode}` | Generate QR code |
| DELETE | `/api/urls/{shortCode}` | Delete a URL |

## 💡 Key Implementation Details

- **Multithreading** — Click events are logged asynchronously using
  `ExecutorService` with a fixed thread pool of 10 threads,
  ensuring redirects are never slowed down by logging operations
- **Base62 Short Codes** — Random 6-character codes generated from
  62 possible characters with uniqueness guaranteed by DB check
- **QR Code** — Generated using Google's ZXing library,
  returned as PNG image bytes
- **Link Expiry** — Expiry date stored in DB,
  checked on every redirect request

## 👨‍💻 Author
Vansh Agarwal— [Hackergeu](https://github.com/Hackergeu)
