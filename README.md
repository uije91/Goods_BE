# 중고거래 플랫폼 굿즈(Goods)
2024년 5월부터 7월 까지 약 2달간 진행한 팀프로젝트입니다.
<br><br>

## 📝 프로젝트 기획
물가 상승으로 인해 중고거래 수요가 증가하고 있습니다.<br>
택배 거래는 배송 기간과 배송비가 추가된다는 점에서 근거리 거래를 희망하는 사람들이 많습니다.<br>
그럴 때 필요한 물건을 내 위치 기준 바로 이웃에게 구매할 수 있는 플랫폼을 만들어보고자 프로젝트를 기획했습니다.

## 🔧 사용 기술
<div>
  <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat-square&logo=SpringBoot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=SpringSecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=Gradle&logoColor=white"/>
</div>
<div>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/elasticsearch-005571?style=flat-square&logo=elasticsearch&logoColor=white"/>
</div>
<div>
  <img src="https://img.shields.io/badge/Amazon EC2-FF9900?style=flat-square&logo=Amazon EC2&logoColor=white"/>
  <img src="https://img.shields.io/badge/Amazon S3-569A31?style=flat-square&logo=Amazon S3&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=Docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/Github Actions-2088FF?style=flat-square&logo=Github Actions&logoColor=white"/>
</div>

## 🙋‍♂️ 나의 담당 역할

**🙎‍♀ 회원**

- **회원 가입**
    - Goods 웹페이지에서 **DB로 직접 저장**되는 회원가입을 맡았습니다. **이메일을 통한 사용자 인증**과 **닉네임 중복 검사**를 통해 유효성을 검사하였습니다.
- **이메일 인증과 인증 확인**
    - **JavaMailSender**를 통해 이메일을 생성하여 사용자에게 보냈습니다.
    - **redis**를 이용해서 인증번호를 유효시간을 설정하여 임시저장하고 사용자의 입력 값과 비교하여 인증 확인을 진행하였습니다.
- **회원 탈퇴**
    - 이용하지 않는 고객을 기존 작성정보는 유지, 활성 상태만 비활성으로 돌려 **soft-delete** 방식으로 회원 탈퇴를 구현하였습니다.

**🛍️ 상품**

- **상품 등록**
    - 상품명, 가격, 설명, 위치, 이미지를 등록할 수 있게 구성하였습니다.
    - 이미지 파일은 Goods 관리자 누구든 접근해서 확인할 수 있고 용량 누적에 따른 유연한 관리를 위해 **aws S3**에 저장할 수 있게 설정하였습니다.
- **상품 상세보기**
    - 상품 id를 식별하여 상품의 기존 정보 뿐만 아니라 거래 상태, 판매자의 정보등을 담은 상세 정보를 넘겨주는 기능을 구현하였습니다.

**🔍 검색**

- **상품 검색**
    - **Elastic Search**의 `NativeSearchQueryBuilder`를 이용하여 사용자가 입력한 검색어를 상품 등록 시 작성한 상품명, 설명, 위치 등에서 유사도가 높은 검색 결과를 반환하게 구현하였습니다.
    - 사용자가 상품을 등록 완료 시 **Elastic Search의 DB**에 데이터를 저장하게 하였습니다. 서버를 재가동했을 때에도 **기존 데이터가 유지되게 volume**을 사용했습니다.
    - 거래 완료가 되거나 상품을 삭제할 경우 Elastic Search DB에서도 같이 제거하여, 검색 **동기화**를 맞출려고 했습니다.
- **내 위치 주변 상품 검색**
    - 내 위치를 받아와서 주변에 위치한 상품들을 좋아요 순으로 가져옵니다.
    - **Elastic Search**의 `*geoDistanceQuery*`를 이용하여 사용자의 위치를 기준으로 **근처 2km 반경**의 상품들을 검색결과로 반환되게 설정하였습니다.

**💳 거래**

- **거래 시 포인트 송금 및 거래 내역 조회**
    - 구매자와 판매자가 만나서 직접 대면 거래할 때 포인트를 송금하고 **포인트 거래 내역**(송신자, 수신자, 거래금, 거래 일시, 잔액) 등의 정보를 **기록**하게 구현하였습니다.
    - 사용자가 자신의 **구매 및 판매내역**을 잔액 변동과 함께 **추적**할 수 있도록 거래 내역을 **거래 목적 및 형태**와 함께 **조회**할 수 있는 기능을 작성하였습니다.
- **거래 후 별점 작성**
    - 거래를 완료한 상품에 대해서 구매자가 판매자 및 상품에 대한 **후기를 별점으로 남길 수 있는 기능**으로 추후 다른 사용자들이 해당 판매자의 매너나 신뢰도를 확인할 수 있습니다.

**🧭 알림**

- **입금 확인 알림 전송, 채팅 수신 알림 전송**
    - **Firebase Cloud Messaging (FCM)**을 이용하여 무료로 안전적으로 원하는 메시지를 전송할 수 있게 구현하였습니다.
    - 알림 사용자 동의와 함께 서버에서 **fcm 토큰을 획득**하고 저장합니다.
    - 위의 **이벤트가 발생 시마다** 서버 자체에서 토큰을 이용해서 firebase 서버로부터 메시지를 사용자의 기기로 전송해달라고 요청합니다.

### ✏️ 커밋 컨벤션

개인 프로젝트와 달리 팀 프로젝트 (협업) 진행을 위해 커밋 및 Pull Request, Git flow, Code Convention을 세부적으로 정하고 개발을 진행하였습니다. 

- 📌Commit Convention
    
    ### 1. 커밋 유형 지정
    
    | 커밋 유형 | 의미 |
    | --- | --- |
    | feat | 새로운 기능 추가 |
    | fix | 버그 수정 |
    | design | CSS 등 사용자 UI 디자인 변경 |
    | docs | 문서 수정 |
    | refactor | 코드 리팩토링 |
    | test | 테스트 코드, 리팩토링 테스트 코드 추가 |
    | chore | 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore |
    | comment | 필요한 주석 추가 및 변경 |
    | !HOTFIX | 급하게 치명적인 버그를 고쳐야 하는 경우 |
    
    ### 2. 제목과 본문을 빈행으로 분리
    
    - 커밋 유형 이후 제목과 본문으로 한글로 작성하여 내용이 잘 전달될 수 있도록 할 것
    - 제목은 영문 기준 50자 이내로 적기
    - 제목 첫글자는 대문자로 적기
    - 제목 끝에 `.` 는 금지
    - 본문에는 변경한 내용과 이유 설명(무엇&왜 설명)
    - CLI에서 커밋 메세지를 여러 줄로 작성하는 방법
        - 쌍따옴표를 닫지 말고 개행하며 작성 → 다 작성한 후에 쌍따옴표를 닫으면 작성 완료
            
            ```jsx
            git commit -m "Feat: 회원가입 기능 추가
            
            - 회원가입 기능 추가"
            ```
            
    
    ### 3. 되도록이면 한 커밋에는 한가지 문제만 작성
    
    - 추적 가능하게 유지해주기
    - 여러가지 항목이 있다면 글머리 기호를 통해 가독성 높이기
- 📌PR Convention
    
     [X]로 수정시 체크박스 활성화
    
    ```markup
    ### PR 유형(어떤 변경 사항이 있나요?)
    - [ ] 새로운 기능 추가
    - [ ] 버그 수정
    - [ ] CSS 등 사용자 UI 디자인 변경
    - [ ] 코드에 영향을 주지 않는 변경사항(오타 수정, 탭 사이즈 변경, 변수명 변경)
    - [ ] 코드 리팩토링
    - [ ] 주석 추가 및 수정
    - [ ] 문서 수정
    - [ ] 테스트 추가, 테스트 리팩토링
    - [ ] 빌드 부분 혹은 패키지 매니저 수정
    - [ ] 파일 혹은 폴더명 수정
    - [ ] 파일 혹은 폴더 삭제
    
    ### 반영 브랜치
    ex) feat/login -> dev
    
    ### 변경 사항
    ex) 로그인 시, 구글 소셜 로그인 기능을 추가했습니다.
    
    ### PR Checklist
    PR이 다음 요구 사항을 충족하는지 확인하세요.
    
    - [ ] 커밋 메시지 컨벤션에 맞게 작성했습니다.
    - [ ] 변경 사항에 대한 테스트를 했습니다.(버그 수정/기능에 대한 테스트)
    ```
    
- 📌Git flow
    
    
    | 브랜치명 | 기능 |
    | --- | --- |
    | main | 초기 세팅 존재 |
    | develop | local 작업 완료 후 merge 브랜치 |
    | feat/기능 | 특정한 기능(단위 기능) 구현 브랜치
    예시) feat/signup - 회원가입 개발 브랜치 |
- 📌Code Convention
    - [Google Java Style Guide](https://newwisdom.tistory.com/96)
    - [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- 활용 예시
    - Commit
        
        ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/d73a059d-ce15-4863-a9a2-7aab66d3fa5f/7717aa29-444f-4eb5-919f-703d6af0d624/Untitled.png)
        
    - PR(Pull Request)
        
        ![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/d73a059d-ce15-4863-a9a2-7aab66d3fa5f/3e8146bb-7d6e-481a-ad97-474044823ec9/Untitled.png)
        

### 🎯 결과 및 성과

결과 정리 ppt 링크 

https://drive.google.com/drive/u/0/folders/1JIYOMQHFkzeRnPXIsHffJ5QIKO4mXjTJ
