# circuit-breaker

## server.js

- **Error Mode X**
    - 10초 동안
    - 100% 확률로 http status code `200` 응답
- **Error Mode O**
    - 10초 동안
    - 20% 확률로 http status code `500` 응답
    - 10% 확률로 http status code `400` 응답
    - 70% 확률로 http status code `200` 응답
- 두 가지 모드가 10초마다 전환

## circuit-breaker

- count_based 로 동작
- server.js `/api/random-error` 로 request 1,000 번 전송
- 실패율 조절하며 circuit breaker 구현
