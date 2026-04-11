---
mode: agent
description: "Use when: writing tests, creating test classes, implementing unit tests, integration tests, or any test-related code"
---

# Testing Best Practices

## Test Structure - AAA Pattern

```java
@Test
@DisplayName("Should return cached geolocation when IP exists in cache")
void shouldReturnCachedGeolocationWhenIpExistsInCache() {
    // Arrange (Given)
    var ip = "8.8.8.8";
    var cachedResult = createGeolocationInfo(ip);
    when(cache.get(ip)).thenReturn(Optional.of(cachedResult));

    // Act (When)
    var result = service.locate(ip);

    // Assert (Then)
    assertThat(result.source()).isEqualTo("cache");
    verify(externalProvider, never()).lookup(any());
}
```

## Naming Conventions

```java
// Test class: <ClasseTestada>Test
class GeolocationServiceTest {}

// Test methods: should_<expected>_when_<condition>
// Ou: given_<context>_when_<action>_then_<result>
void shouldReturnFallback_whenExternalApiFails() {}
void givenPrivateIp_whenLocate_thenReturnBrazilFallback() {}
```

## Test Organization

```
src/test/java/
├── unit/
│   └── application/
│       └── service/
│           └── GeolocationServiceTest.java
├── integration/
│   └── infrastructure/
│       ├── adapter/
│       │   ├── in/
│       │   │   └── web/
│       │   │       └── GeolocationControllerIT.java
│       │   └── out/
│       │       └── client/
│       │           └── IpApiClientIT.java
│       └── cache/
│           └── CaffeineCacheIT.java
└── e2e/
    └── GeolocationE2ETest.java
```

## Unit Tests - Service Layer

```java
@ExtendWith(MockitoExtension.class)
class GeolocationServiceTest {

    @Mock
    private GeolocationProvider provider;

    @Mock
    private GeolocationCache cache;

    @InjectMocks
    private GeolocationService service;

    @Nested
    @DisplayName("IP Validation")
    class IpValidation {
        @ParameterizedTest
        @ValueSource(strings = {"192.168.1.1", "10.0.0.1", "172.16.0.1"})
        void shouldReturnFallbackForPrivateIps(String privateIp) {
            var result = service.locate(privateIp);
            assertThat(result.country().code()).isEqualTo("BR");
        }
    }
}
```

## Integration Tests - Controller

```java
@WebMvcTest(GeolocationController.class)
class GeolocationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeolocationUseCase useCase;

    @Test
    void shouldReturn400WhenIpFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/geolocation/v1/locate")
                .param("ip", "invalid-ip")
                .header("x-device-platform", "Web"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_IP_FORMAT"));
    }

    @Test
    void shouldReturn400WhenPlatformHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/geolocation/v1/locate")
                .param("ip", "8.8.8.8"))
            .andExpect(status().isBadRequest());
    }
}
```

## Integration Tests - HTTP Client (WireMock)

```java
@WireMockTest(httpPort = 8089)
class IpApiClientIT {

    @Test
    void shouldParseExternalApiResponse(WireMockRuntimeInfo wmInfo) {
        stubFor(get(urlEqualTo("/json/8.8.8.8"))
            .willReturn(okJson("""
                {
                    "status": "success",
                    "country": "United States",
                    "countryCode": "US"
                }
                """)));

        var client = new IpApiClient(wmInfo.getHttpBaseUrl());
        var result = client.lookup("8.8.8.8");

        assertThat(result.country().code()).isEqualTo("US");
    }
}
```

## Test Coverage Requirements

- **Mínimo 80%** cobertura na camada de serviço
- 100% de cobertura em validações de IP
- Testar todos os cenários de erro e fallback
- Testar cache hit e cache miss

## Test Data Builders

```java
public class GeolocationTestBuilder {
    public static GeolocationInfo.GeolocationInfoBuilder defaultGeolocation() {
        return GeolocationInfo.builder()
            .ip("8.8.8.8")
            .country(new Country("US", "United States"))
            .city("Mountain View")
            .source("api");
    }

    public static GeolocationInfo brazilFallback(String ip) {
        return GeolocationInfo.builder()
            .ip(ip)
            .country(new Country("BR", "Brazil"))
            .source("fallback")
            .build();
    }
}
```

## Assertions com AssertJ

```java
// Prefer fluent assertions
assertThat(result)
    .isNotNull()
    .satisfies(r -> {
        assertThat(r.country().code()).isEqualTo("BR");
        assertThat(r.source()).isEqualTo("fallback");
    });

// Collections
assertThat(errors)
    .hasSize(1)
    .extracting(Error::code)
    .containsExactly("INVALID_IP_FORMAT");
```
