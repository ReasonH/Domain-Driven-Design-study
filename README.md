## Domain-Driven-Design Study

도메인 주도 설계에 대해 공부한 것을 기록하며 코드 및 테스트 케이스를 작성해봅니다.  
해당 Repository 의 정리 내용은
[이터니티님의 블로그](http://aeternum.egloos.com/category/Domain-Driven%20Design)를 읽고 참조하고 있습니다.  

학습용으로 공부한 내용을 Markdown 형식으로 정리했으며 중간중간 내용을 요약한 부분이 있습니다. 제 기준으로 이해가 막혔던 부분은 내용 중 첨언해두었습니다. 테스트 환경은 Junit 을 이용하며 초기에는 간단한 테스트를 진행, 후에는 Spring 환경에서 실습합니다.

지식 공유보다는 학습을 목적으로 하기에 이론 및 개념은 원본과 동일한 내용입니다.

실습은 작성 시점의 구현환경에 맞게 변경했습니다.

- 실습 환경, 테스트 환경 - Spring / Junit 버전 등
- 원문 실습 코드에 포함되지 않는 예외처리용 코드 등
- DB: inmemory H2
- Hibernate mapping -> JPA로 일괄 대체
- XML -> Annotation 일괄 대체

---
### 목차

Chapter01. [Value Object와 Reference Object](studyhistory/Chapter01.md)

Chapter02. [Aggregate와 Repository](studyhistory/Chapter02.md)  

Chapter03-1. [영속성 관리](studyhistory/Chapter03-1.md)

Chapter03-2. [Dependency injection과 AOP](studyhistory/Chapter03-2.md)

Chapter04-1. [ORM과 투명한 영속성 - Entity 생명주기와 식별자](studyhistory/Chapter04-1.md)

Chapter04-2. [ORM과 투명한 영속성 - Hibernate ORM](studyhistory/Chapter04-2.md)

Chapter04-3. [ORM과 투명한 영속성 - 마무리](studyhistory/Chapter04-3.md)