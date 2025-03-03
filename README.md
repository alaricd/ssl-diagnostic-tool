# SSL Diagnostic Tool

`ssl-diagnostic-tool` is a command-line tool to diagnose SSL/TLS certificate issues. It helps developers and operators quickly understand:

- Why certificate validation might fail
- Whether certificates are expired, self-signed, or missing from the truststore
- Whether the full certificate chain is present
- Which truststores (system or internal) trust each certificate

## Features

- Supports **system truststore (`cacerts`)**.
- Supports **internal truststore (corporate JKS files)**.
- Provides **human-readable diagnostics** in plain English.
- Works as a **standalone JAR** or in **Docker**.
- Easily integratable into CI/CD pipelines.

## Quick Start

### Build

```bash
./build.sh
```

### Run

```bash
# With internal truststore
export TRUSTSTORE_PATH=/path/to/custom-truststore.jks
export TRUSTSTORE_PASSWORD=changeit

./run.sh https://example.com
```

### Docker

```bash
docker build -t ssl-diagnostic-tool .
docker run --rm -e TRUSTSTORE_PATH=/path/to/custom-truststore.jks -e TRUSTSTORE_PASSWORD=changeit ssl-diagnostic-tool https://example.com
```

## Configuration

| Property | Description |
|---|---|
| `internal.truststore.path` | Path to your internal truststore (JKS file) |
| `internal.truststore.password` | Password for the internal truststore |
| `javax.net.ssl.trustStore` | (Optional) Override system truststore path |
| `javax.net.ssl.trustStorePassword` | (Optional) Override system truststore password |

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository.
2. Create a new branch.
3. Make your changes.
4. Submit a pull request.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
