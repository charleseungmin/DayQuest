# Character Graphics Audit - 2026-05-05

## Objective
- 캐릭터 육성 기능에 직접 보이는 그래픽 표현을 빠르게 추가한다.
- Krita/PNG 에셋 제작을 기다리지 않고 현재 앱에서 바로 렌더링되는 구현을 우선한다.

## Implementation
- `DayQuestWireframe.kt`의 Today `CharacterGrowthCard`에 `CharacterAvatar`를 추가했다.
- `CharacterAvatar`는 Compose `Canvas`로 캐릭터를 그린다.
- 레벨 구간별 그래픽 단계를 적용했다.
  - Lv.1~3: 기본 모험가
  - Lv.4~6: 오라/망토
  - Lv.7~9: 지팡이/장비
  - Lv.10+: 왕관/강화 오라
- 집중/체력/통찰/균형 스탯은 캐릭터 하단 색상 오브로 표시한다.
- TalkBack용 `contentDescription`을 추가했다.

## Scope Decisions
- 이번 반복에서는 외부 아트 툴 파일, PNG, 애니메이션 파이프라인을 추가하지 않는다.
- 추후 Krita 에셋이 준비되면 `CharacterAvatar` 내부를 이미지 렌더러로 교체할 수 있게 UI 진입점을 분리해 둔다.

## Validation
- `:app:compileDebugKotlin`: PASS
- `scripts/verify.sh`: PASS
- `scripts/check_done.sh`: PASS
