# CoreBundle — 서버 전용 통합 플러그인

Minecraft 1.20+ (Paper/Spigot) 하드코어 야생 서버를 위한 올인원 플러그인입니다.

## 빌드 방법

```bash
mvn clean package
```

빌드가 끝나면 `target/corebundle-1.0.0.jar` 파일을 서버의 `plugins/` 폴더에 넣고,
Vault 플러그인(https://www.spigotmc.org/resources/vault.34315/) 및 실제 경제 플러그인
(EssentialsX 등)을 함께 설치하세요. Vault가 없어도 플러그인은 정상 작동하지만
소지금 표시·낚시 판매·몹 보상 기능은 비활성화됩니다.

## 기능별 요약

| 요청 기능 | 구현 방식 |
|---|---|
| ① 스타터 킷 미지급 | 최초 접속 시 인벤토리/엔더상자 강제 초기화 |
| ② 스폰 보호구역 | 중심좌표+반경 기반 경량 구역 판정, `/coreadmin setcenter`/`setradius` |
| ③ 스코어보드/Tab | 사이드바 소지금(Vault)·접속자수 표시, 설정된 주기(기본 1초)로만 갱신 |
| ④ 미니맵 | 아래 "미니맵 관련 안내" 참고 |
| ⑤ 커스텀 낚시 | 확률 기반 등급(일반/레어/전설) 부여, `/fish sell`로 Vault 정산 |
| ⑥ 커스텀 몹 | 야간 확률 스폰 시 체력/공격력 배수 적용 + 네임드, 처치 보상(돈/경험치/전리품) |
| ⑦ 귀환석 | PAPER + CustomModelData(10001), 우클릭 즉시 스폰 이동, 파티클/사운드, 소모, 쿨타임 |

## 명령어 / 권한

- `/coreadmin setcenter` — 현재 위치를 스폰 보호구역 중심으로 지정 (`corebundle.admin`)
- `/coreadmin setradius <반경>` — 보호구역 반경 설정 (`corebundle.admin`)
- `/coreadmin info` / `/coreadmin reload`
- `/fish sell` — 인벤토리 내 커스텀 물고기 일괄 판매
- `/coreadmin give recall <닉네임>` — 귀환석 아이템 지급 (`corebundle.admin`)
- `corebundle.bypass` — 스폰 보호구역 제한 무시 (기본 OP)

## 귀환석 참고사항

- 이동 목적지는 `/coreadmin setcenter`로 지정한 **스폰 보호구역 중심 좌표**입니다.
  world spawn과 다른 값을 쓰고 싶다면 `RecallStoneListener.teleportToSpawn()`에서
  좌표 소스만 바꾸면 됩니다.
- CustomModelData(10001)는 리소스팩에서 PAPER 아이템에 해당 모델을 매핑해야
  실제 텍스처가 바뀝니다. 리소스팩이 없으면 일반 종이 아이템처럼 보이되
  이름/로어/기능은 동일하게 작동합니다.
- 쿨타임은 플레이어별로 메모리에서만 관리되며 서버 재시작 시 초기화됩니다.

## 스폰 보호구역에 대한 참고

요청서에 `/rg define spawn`(WorldGuard 스타일 명령어)이 언급되어 있었는데,
WorldGuard는 별도의 대형 플러그인이라 "불필요한 대형 라이브러리 제외" 요청과
맞지 않아 자체 경량 구역 시스템(중심좌표+반경)으로 대체했습니다. 다각형/직육면체
형태의 복잡한 구역이 필요하시면 WorldGuard를 함께 설치하고 이 플러그인의
`SpawnProtectionListener`를 WorldGuard의 `ApplicableRegionSet` 체크로 바꾸는 방식이
더 적합합니다. 필요하시면 그 버전도 만들어 드릴 수 있습니다.

## 미니맵 관련 안내 (중요)

Xaero's Minimap, JourneyMap 같은 클라이언트 모드는 **클라이언트가 이미 받은
청크 데이터를 클라이언트 자체에서 렌더링**하는 방식입니다. 즉:

- 바닐라 서버에서도 특별한 설정 없이 정상 작동하며, 서버 플러그인이 "패킷을
  중계"하거나 "호환 처리"를 해줄 필요가 없습니다.
- 서버가 통제할 수 있는 부분은 ⓐ 좌표 정보를 얼마나 멀리까지/얼마나 자주
  클라이언트에 노출할지(예: view-distance, 좌표 표시 여부), ⓑ 안티치트가
  이런 모드들을 오탐지하지 않도록 예외처리하는 정도입니다.
- 이번 코드에는 그래서 서버 플러그인 차원에서 실제로 구현 가능한 대안으로
  **좌표(X/Y/Z) + 8방위 액션바 HUD**(`CoordHudTask`)를 넣었습니다. 하드코어
  컨셉상 좌표 노출 자체를 막고 싶다면 `config.yml`의 `coord-hud.enabled`를
  `false`로 끄면 됩니다.
- "지형이 그려지는" 진짜 미니맵이 필요하다면 플러그인이 아니라 클라이언트에
  Xaero's Minimap/JourneyMap을 설치하도록 안내하는 것이 맞고, 서버 전체 지도를
  웹으로 보고 싶다면 **Dynmap**이나 **BlueMap** 같은 별도 플러그인을 추가로
  설치하는 방식을 권장드립니다 (둘 다 대형 라이브러리라 이 번들에는 포함하지
  않았습니다).

## config.yml로 조정 가능한 값

- `spawn-region`: world, center-x/y/z, radius, enabled
- `scoreboard`: update-interval-ticks, server-name, title, tab-header/footer
- `coord-hud`: enabled, update-interval-ticks
- `fishing`: chance-common/rare/legendary(%), price-common/rare/legendary, size-min/max-cm
- `custom-mobs`: night-only, spawn-chance-percent, health-multiplier, damage-multiplier, reward-money, reward-exp
