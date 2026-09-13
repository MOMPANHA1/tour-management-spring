# Tour Management — Pseudo-code ពេញលេញ (៩ Features)

> ឯកសារនេះជា **ផែនការតក្កវិជ្ជា (logic blueprint)** មុនពេលសរសេរកូដ Java។
> សរសេរជាភាសាមនុស្ស មិនមែន Java syntax។ ពេលកូដរួច ត្រូវត្រឡប់មកប្រៀបធៀបវិញ។
>
> - **Base package** : `co.panha.hibernate.tourmanagement`
> - **Stack** : Spring Boot 4.1.1 · Java 25 · Spring Data JPA · Spring WebMVC · PostgreSQL · Lombok
> - **API prefix** : `/api/v1`

---

## មាតិកា

| # | Feature | Package | ស្មុគស្មាញ |
|---|---------|---------|-----------|
| [០](#០-បទដ្ឋានរួម-conventions) | បទដ្ឋានរួម (Conventions) | — | — |
| [F1](#f1--category-ប្រភេទ-tour) | Category | `features.category` | ងាយ (CRUD) |
| [F2](#f2--destination-ទីតាំងគោលដៅ) | Destination | `features.destination` | ងាយ (CRUD) |
| [F3](#f3--guide-មគគទទសក) | Guide | `features.guide` | មធ្យម |
| [F4](#f4--tour-កញចប់ដណរកមសានត) | Tour | `features.tour` | មធ្យម |
| [F5](#f5--schedule-កាលវភាគចញដណរ) | Schedule | `features.schedule` | មធ្យម |
| [F6](#f6--customer-អតថជន) | Customer | `features.customer` | មធ្យម |
| [F7](#f7--booking-ការកក) | Booking | `features.booking` | **ខ្ពស់** |
| [F8](#f8--payment-ការទទាត) | Payment | `features.payment` | **ខ្ពស់** |
| [F9](#f9--review-ការវាយតមល) | Review | `features.review` | មធ្យម |
| [X](#x--cross-cutting-ផនកឆលងកាត) | Cross-cutting | `exception`, `security`, `utils` | — |

---

## ០. បទដ្ឋានរួម (Conventions)

### ០.១ Keyword ដែលប្រើក្នុងឯកសារនេះ

```
FUNCTION name(args) -> ReturnType     ចាប់ផ្តើម method
END FUNCTION                          បញ្ចប់ method
IF ... THEN ... ELSE ...              លក្ខខណ្ឌ
FOR EACH x IN list                    រង្វិល
SET x = value                         ផ្តល់តម្លៃ
CALL service.method()                 ហៅសេវាផ្សេង
RETURN value                          ត្រឡប់លទ្ធផល
THROW <ErrorType>("សារខ្មែរ")          បោះកំហុស
ORELSE THROW <ErrorType>("...")       Optional ទទេ → បោះកំហុស
@Query name(args) -> Type              custom repository query
# ...                                 មតិយោបល់ពន្យល់
```

### ០.២ លំដាប់ ៨ ជំហានស្តង់ដារ

រាល់ business function ត្រូវសរសេរតាមលំដាប់នេះ (ជំហានណាមិនចាំបាច់ អាចរំលង)៖

```
១. Validate        ផ្ទៀងផ្ទាត់ទិន្នន័យចូល
២. Load            ទាញធនធានពី database
៣. Check Rules     ពិនិត្យវិន័យអាជីវកម្ម
៤. Calculate       គណនា
៥. Build           បង្កើត/កែ entity
៦. Save            រក្សាទុក
៧. Side Effects    ផលរំកិល (email, update statistics)
៨. Return          ត្រឡប់លទ្ធផល
```

### ០.៣ តួនាទី Layer

| Layer | ធ្វើអ្វី | **មិន**ធ្វើអ្វី |
|---|---|---|
| Controller | ទទួល HTTP · `@Valid` · ហៅ Service · ត្រឡប់ Response | គ្មាន business logic · មិនប៉ះ Repository |
| Service | Business rule · គណនា · `@Transactional` | មិនស្គាល់ HTTP |
| Repository | Query database | គ្មាន logic |
| Mapper | Entity ↔ DTO | មិនហៅ database |
| DTO | រូបរាងទិន្នន័យ + validation | គ្មាន logic |
| Entity | រូបរាងតារាង | មិនត្រឡប់ចេញ API ផ្ទាល់ |

### ០.៤ Error Type → HTTP Status

| Pseudo-code | HTTP | ប្រើពេលណា |
|---|---|---|
| `THROW ResponseStatusException(HttpStatus.BAD_REQUEST, ...)` | 400 | ទិន្នន័យចូលមិនត្រឹមត្រូវ |
| `THROW ResponseStatusException(HttpStatus.UNAUTHORIZED, ...)` | 401 | មិនទាន់ login |
| `THROW ResponseStatusException(HttpStatus.FORBIDDEN, ...)` | 403 | login ហើយ តែគ្មានសិទ្ធិ |
| `THROW ResponseStatusException(HttpStatus.NOT_FOUND, ...)` | 404 | រកមិនឃើញធនធាន |
| `THROW ResponseStatusException(HttpStatus.CONFLICT, ...)` | 409 | ស្ទួន · ស្ថានភាពមិនអនុញ្ញាត · អស់កៅអី |
| `THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, ...)` | 422 | ត្រឹមត្រូវតាមទម្រង់ តែខុសវិន័យអាជីវកម្ម |

### ០.៥ Field រួមគ្រប់ Entity (Base Auditing)

```
ABSTRACT ENTITY BaseEntity  @MappedSuperclass @EntityListeners(AuditingEntityListener)
    id          : Long           @Id @GeneratedValue(IDENTITY)
    uuid        : String         @Column(unique, nullable=false, length=36)
    isDeleted   : Boolean        @Column(nullable=false)  DEFAULT false
    createdAt   : LocalDateTime  @CreatedDate
    updatedAt   : LocalDateTime  @LastModifiedDate
```

> - `uuid` ជាសោសាធារណៈដែលបង្ហាញលើ API (មិនបង្ហាញ `id`)។
> - `isDeleted` សម្រាប់ **soft delete** — គ្រប់ query ត្រូវច្រោះ `isDeleted = false`។
> - ត្រូវបើក `@EnableJpaAuditing` នៅ `config/JpaAuditingConfig`។

### ០.៦ លំនាំ Paging រួម

```
DTO PageResponse<T>:
    content        : List<T>
    page           : int
    size           : int
    totalElements  : long
    totalPages     : int
    isFirst        : boolean
    isLast         : boolean
```

```
FUNCTION buildPageable(page, size, sortBy, direction) -> Pageable:
    IF page < 0 THEN SET page = 0
    IF size <= 0 OR size > 100 THEN SET size = 10
    sort = Sort.by(direction, sortBy ORELSE "createdAt")
    RETURN PageRequest.of(page, size, sort)
END FUNCTION
```

### ០.៧ រចនាសម្ព័ន្ធ Package

```
co.panha.hibernate.tourmanagement
├── TourManagementApplication.java
├── config/           JpaAuditingConfig, OpenApiConfig, WebConfig
├── exception/        GlobalAppException, ApiErrorResponse
├── security/         SecurityConfig, AuthUtils
├── utils/            GenerateUtils, DateUtils
├── base/             BaseEntity, PageResponse, PageMapper
└── features/
    ├── category/     Category, Repository, Service, ServiceImpl, Controller, Mapper, dto/
    ├── destination/
    ├── guide/
    ├── tour/
    ├── schedule/
    ├── customer/
    ├── booking/
    ├── payment/
    └── review/
```

### ០.៨ ផែនទីទំនាក់ទំនង (ERD សង្ខេប)

```
Category  1 ────< N  Tour
Tour      N >───< N  Destination        (join table: tour_destinations)
Tour      1 ────< N  TourSchedule
Guide     1 ────< N  TourSchedule
Customer  1 ────< N  Booking
Schedule  1 ────< N  Booking
Booking   1 ────< N  Payment
Booking   1 ────  1  Review
Tour      1 ────< N  Review
Customer  1 ────< N  Review
```

---

# F1 — Category (ប្រភេទ Tour)

**គោលបំណង** : ចាត់ថ្នាក់ Tour ជាក្រុម (ធម្មជាតិ · វប្បធម៌ · សមុទ្រ · ផ្សងព្រេង)។

## F1.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC1.1 | Admin បង្កើតប្រភេទ | `POST /api/v1/categories` | ADMIN |
| UC1.2 | មើលបញ្ជីប្រភេទទាំងអស់ | `GET /api/v1/categories` | សាធារណៈ |
| UC1.3 | មើលប្រភេទមួយ | `GET /api/v1/categories/{uuid}` | សាធារណៈ |
| UC1.4 | Admin កែប្រភេទ | `PATCH /api/v1/categories/{uuid}` | ADMIN |
| UC1.5 | Admin លុបប្រភេទ | `DELETE /api/v1/categories/{uuid}` | ADMIN |

## F1.2 Entity

```
ENTITY Category EXTENDS BaseEntity  -> table "categories"
    name        : String   @Column(unique, nullable=false, length=80)
    slug        : String   @Column(unique, nullable=false, length=100)
    description : String   @Column(columnDefinition = "TEXT")
    iconUrl     : String   @Column(length=255)
    tours       : List<Tour>  @OneToMany(mappedBy="category")
```

## F1.3 DTOs

```
DTO CreateCategoryRequest:
    name        : String  @NotBlank @Size(max=80)
    description : String  @Size(max=1000)
    iconUrl     : String  @Size(max=255)

DTO UpdateCategoryRequest:          # PATCH — គ្រប់ field អាច null
    name        : String  @Size(max=80)
    description : String  @Size(max=1000)
    iconUrl     : String  @Size(max=255)

DTO CategoryResponse:
    uuid        : String
    name        : String
    slug        : String
    description : String
    iconUrl     : String
    tourCount   : long          # ចំនួន Tour ក្នុងប្រភេទនេះ
```

## F1.4 Repository

```
REPOSITORY CategoryRepository EXTENDS JpaRepository<Category, Long>

    findByUuidAndIsDeletedFalse(uuid)       -> Optional<Category>
    existsByNameIgnoreCaseAndIsDeletedFalse(name) -> boolean
    existsBySlug(slug)                      -> boolean
    findAllByIsDeletedFalse(pageable)       -> Page<Category>

    @Query countActiveTours(categoryId) -> long
        JPQL: SELECT COUNT(t) FROM Tour t
             WHERE t.category.id = :categoryId AND t.isDeleted = false
```

## F1.5 Service

```
FUNCTION createNew(req) -> CategoryResponse:

    # ១. Validate (ធ្វើដោយ @Valid រួចហើយ)

    # ៣. Check Rules
    IF categoryRepo.existsByNameIgnoreCaseAndIsDeletedFalse(req.name) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ប្រភេទឈ្មោះ '" + req.name + "' មានរួចហើយ")

    # ៥. Build
    category = mapper.toEntity(req)
    SET category.uuid      = randomUUID()
    SET category.slug      = generateUniqueSlug(req.name)
    SET category.isDeleted = false

    # ៦. Save + ៨. Return
    RETURN mapper.toResponse(categoryRepo.save(category))

END FUNCTION


FUNCTION generateUniqueSlug(name) -> String:
    base = toLowerCase(name)
           REPLACE spaces WITH "-"
           REMOVE non-alphanumeric-except-dash
    SET slug = base
    SET counter = 1
    WHILE categoryRepo.existsBySlug(slug) DO
        SET slug = base + "-" + counter
        SET counter = counter + 1
    RETURN slug
END FUNCTION


FUNCTION findAll(page, size) -> PageResponse<CategoryResponse>:
    pageable = buildPageable(page, size, "name", ASC)
    result   = categoryRepo.findAllByIsDeletedFalse(pageable)
    RETURN PageResponse.from(result MAP mapper::toResponse)
END FUNCTION


FUNCTION findByUuid(uuid) -> CategoryResponse:
    category = categoryRepo.findByUuidAndIsDeletedFalse(uuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញប្រភេទ uuid = " + uuid)
    RETURN mapper.toResponse(category)
END FUNCTION


FUNCTION updateByUuid(uuid, req) -> CategoryResponse:

    category = categoryRepo.findByUuidAndIsDeletedFalse(uuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញប្រភេទ uuid = " + uuid)

    # ពិនិត្យស្ទួន — តែបើឈ្មោះពិតជាប្តូរ
    IF req.name IS NOT NULL AND req.name != category.name THEN
        IF categoryRepo.existsByNameIgnoreCaseAndIsDeletedFalse(req.name) THEN
            THROW ResponseStatusException(HttpStatus.CONFLICT, "ប្រភេទឈ្មោះនេះមានរួចហើយ")
        SET category.slug = generateUniqueSlug(req.name)

    mapper.toEntity(req, category)     # update តែ field មិន null
    RETURN mapper.toResponse(categoryRepo.save(category))

END FUNCTION


FUNCTION deleteByUuid(uuid) -> void:

    category = categoryRepo.findByUuidAndIsDeletedFalse(uuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញប្រភេទ uuid = " + uuid)

    # វិន័យ៖ លុបមិនបានបើនៅមាន Tour សកម្ម
    activeTours = categoryRepo.countActiveTours(category.id)
    IF activeTours > 0 THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "មិនអាចលុបបានទេ ព្រោះនៅមាន " + activeTours + " Tour ក្នុងប្រភេទនេះ")

    SET category.isDeleted = true
    categoryRepo.save(category)

END FUNCTION
```

## F1.6 Controller

```
CONTROLLER CategoryController  base = "/api/v1/categories"

    POST   "/"              status 201  @Valid CreateCategoryRequest
           -> RETURN service.createNew(body)

    GET    "/"              status 200  page=0, size=10
           -> RETURN service.findAll(page, size)

    GET    "/{uuid}"        status 200
           -> RETURN service.findByUuid(uuid)

    PATCH  "/{uuid}"        status 200  @Valid UpdateCategoryRequest
           -> RETURN service.updateByUuid(uuid, body)

    DELETE "/{uuid}"        status 204
           -> CALL service.deleteByUuid(uuid)
```

---

# F2 — Destination (ទីតាំងគោលដៅ)

**គោលបំណង** : ទីតាំងដែល Tour ទៅដល់ (សៀមរាប · កែប · មណ្ឌលគិរី · កោះរ៉ុង)។

## F2.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC2.1 | Admin បង្កើតទីតាំង | `POST /api/v1/destinations` | ADMIN |
| UC2.2 | មើលបញ្ជីទីតាំង (ស្វែងរកតាមខេត្ត) | `GET /api/v1/destinations?province=` | សាធារណៈ |
| UC2.3 | មើលទីតាំងមួយ + Tour ដែលទៅដល់ | `GET /api/v1/destinations/{uuid}` | សាធារណៈ |
| UC2.4 | Admin កែទីតាំង | `PATCH /api/v1/destinations/{uuid}` | ADMIN |
| UC2.5 | Admin លុបទីតាំង | `DELETE /api/v1/destinations/{uuid}` | ADMIN |

## F2.2 Entity

```
ENTITY Destination EXTENDS BaseEntity  -> table "destinations"
    name        : String      @Column(nullable=false, length=120)
    province    : String      @Column(nullable=false, length=80)
    country     : String      @Column(nullable=false, length=80)  DEFAULT "Cambodia"
    description : String      @Column(columnDefinition = "TEXT")
    latitude    : BigDecimal  @Column(precision=10, scale=7)
    longitude   : BigDecimal  @Column(precision=10, scale=7)
    imageUrl    : String      @Column(length=255)
    tours       : Set<Tour>   @ManyToMany(mappedBy="destinations")

    CONSTRAINT unique(name, province)
```

## F2.3 DTOs

```
DTO CreateDestinationRequest:
    name        : String      @NotBlank @Size(max=120)
    province    : String      @NotBlank @Size(max=80)
    country     : String      @Size(max=80)
    description : String      @Size(max=2000)
    latitude    : BigDecimal  @DecimalMin("-90")  @DecimalMax("90")
    longitude   : BigDecimal  @DecimalMin("-180") @DecimalMax("180")
    imageUrl    : String      @Size(max=255)

DTO UpdateDestinationRequest:       # គ្រប់ field nullable

DTO DestinationResponse:
    uuid, name, province, country, description
    latitude, longitude, imageUrl
    tourCount   : long

DTO DestinationDetailResponse EXTENDS DestinationResponse:
    tours : List<TourCardResponse>
```

## F2.4 Repository

```
REPOSITORY DestinationRepository EXTENDS JpaRepository<Destination, Long>

    findByUuidAndIsDeletedFalse(uuid)                 -> Optional<Destination>
    existsByNameIgnoreCaseAndProvinceIgnoreCase(n, p) -> boolean
    findAllByIsDeletedFalse(pageable)                 -> Page<Destination>
    findAllByProvinceIgnoreCaseAndIsDeletedFalse(province, pageable) -> Page<Destination>
    findAllByUuidIn(uuids)                            -> List<Destination>
```

## F2.5 Service

```
FUNCTION createNew(req) -> DestinationResponse:

    IF destinationRepo.existsByNameIgnoreCaseAndProvinceIgnoreCase(req.name, req.province) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ទីតាំង '" + req.name + "' នៅខេត្ត " + req.province + " មានរួចហើយ")

    # វិន័យ៖ បើដាក់ latitude ត្រូវដាក់ longitude ផងដែរ
    IF (req.latitude IS NULL) != (req.longitude IS NULL) THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ត្រូវបំពេញ latitude និង longitude ទាំងពីរ ឬទុកទទេទាំងពីរ")

    destination = mapper.toEntity(req)
    SET destination.uuid      = randomUUID()
    SET destination.country   = req.country ORELSE "Cambodia"
    SET destination.isDeleted = false

    RETURN mapper.toResponse(destinationRepo.save(destination))

END FUNCTION


FUNCTION findAll(province, page, size) -> PageResponse<DestinationResponse>:
    pageable = buildPageable(page, size, "name", ASC)

    IF province IS NOT EMPTY THEN
        result = destinationRepo.findAllByProvinceIgnoreCaseAndIsDeletedFalse(province, pageable)
    ELSE
        result = destinationRepo.findAllByIsDeletedFalse(pageable)

    RETURN PageResponse.from(result MAP mapper::toResponse)
END FUNCTION


FUNCTION findByUuid(uuid) -> DestinationDetailResponse:
    destination = destinationRepo.findByUuidAndIsDeletedFalse(uuid)
                  ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញទីតាំង uuid = " + uuid)
    RETURN mapper.toDetailResponse(destination)
END FUNCTION


FUNCTION updateByUuid(uuid, req) -> DestinationResponse:
    destination = destinationRepo.findByUuidAndIsDeletedFalse(uuid)
                  ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញទីតាំង uuid = " + uuid)
    mapper.toEntity(req, destination)
    RETURN mapper.toResponse(destinationRepo.save(destination))
END FUNCTION


FUNCTION deleteByUuid(uuid) -> void:
    destination = destinationRepo.findByUuidAndIsDeletedFalse(uuid)
                  ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញទីតាំង uuid = " + uuid)

    IF destination.tours IS NOT EMPTY THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "មិនអាចលុបបានទេ ព្រោះនៅមាន Tour ភ្ជាប់នឹងទីតាំងនេះ")

    SET destination.isDeleted = true
    destinationRepo.save(destination)
END FUNCTION
```

## F2.6 Controller

```
CONTROLLER DestinationController  base = "/api/v1/destinations"

    POST   "/"           201   @Valid CreateDestinationRequest
    GET    "/"           200   province(optional), page=0, size=10
    GET    "/{uuid}"     200
    PATCH  "/{uuid}"     200   @Valid UpdateDestinationRequest
    DELETE "/{uuid}"     204
```

---

# F3 — Guide (មគ្គុទ្ទេសក៍)

**គោលបំណង** : គ្រប់គ្រងមគ្គុទ្ទេសក៍ និងចាត់ចែងទៅកាលវិភាគ។

## F3.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC3.1 | Admin បង្កើតមគ្គុទ្ទេសក៍ | `POST /api/v1/guides` | ADMIN |
| UC3.2 | Admin មើលបញ្ជីមគ្គុទ្ទេសក៍ | `GET /api/v1/guides` | ADMIN |
| UC3.3 | មើលប្រវត្តិរូបមគ្គុទ្ទេសក៍ | `GET /api/v1/guides/{uuid}` | សាធារណៈ |
| UC3.4 | Admin កែព័ត៌មាន | `PATCH /api/v1/guides/{uuid}` | ADMIN |
| UC3.5 | Admin ប្តូរស្ថានភាព (សកម្ម/ឈប់) | `PATCH /api/v1/guides/{uuid}/status` | ADMIN |
| UC3.6 | រកមគ្គុទ្ទេសក៍ទំនេរក្នុងចន្លោះកាលបរិច្ឆេទ | `GET /api/v1/guides/available` | ADMIN |

## F3.2 Entity

```
ENTITY Guide EXTENDS BaseEntity  -> table "guides"
    code            : String       @Column(unique, nullable=false, length=20)   # GD-0001
    fullName        : String       @Column(nullable=false, length=120)
    gender          : Gender       @Enumerated(STRING)
    phoneNumber     : String       @Column(unique, nullable=false, length=20)
    email           : String       @Column(unique, length=120)
    languages       : Set<String>  @ElementCollection   # ["KH","EN","ZH"]
    yearsExperience : Integer
    bio             : String       @Column(columnDefinition = "TEXT")
    photoUrl        : String       @Column(length=255)
    status          : GuideStatus  @Enumerated(STRING)
    schedules       : List<TourSchedule>  @OneToMany(mappedBy="guide")

ENUM Gender        : MALE, FEMALE, OTHER
ENUM GuideStatus   : ACTIVE, ON_LEAVE, INACTIVE
```

## F3.3 DTOs

```
DTO CreateGuideRequest:
    fullName        : String       @NotBlank @Size(max=120)
    gender          : Gender       @NotNull
    phoneNumber     : String       @NotBlank @Pattern("^0[1-9][0-9]{7,8}$")
    email           : String       @Email @Size(max=120)
    languages       : Set<String>  @NotEmpty
    yearsExperience : Integer      @Min(0) @Max(60)
    bio             : String       @Size(max=2000)
    photoUrl        : String       @Size(max=255)

DTO UpdateGuideStatusRequest:
    status : GuideStatus  @NotNull
    reason : String       @Size(max=255)

DTO GuideResponse:
    uuid, code, fullName, gender, phoneNumber, email
    languages, yearsExperience, bio, photoUrl, status
    assignedScheduleCount : long
```

## F3.4 Repository

```
REPOSITORY GuideRepository EXTENDS JpaRepository<Guide, Long>

    findByUuidAndIsDeletedFalse(uuid)   -> Optional<Guide>
    existsByPhoneNumber(phone)          -> boolean
    existsByEmail(email)                -> boolean
    findAllByStatusAndIsDeletedFalse(status, pageable) -> Page<Guide>
    countByIsDeletedFalse()             -> long

    @Query findAvailableGuides(startDate, endDate) -> List<Guide>
        JPQL: SELECT g FROM Guide g
             WHERE g.status = 'ACTIVE' AND g.isDeleted = false
               AND g.id NOT IN (
                   SELECT s.guide.id FROM TourSchedule s
                   WHERE s.guide IS NOT NULL
                     AND s.status <> 'CANCELLED'
                     AND s.departureDate <= :endDate
                     AND s.returnDate    >= :startDate
               )
```

## F3.5 Service

```
FUNCTION createNew(req) -> GuideResponse:

    IF guideRepo.existsByPhoneNumber(req.phoneNumber) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "លេខទូរស័ព្ទនេះមានក្នុងប្រព័ន្ធរួចហើយ")

    IF req.email IS NOT NULL AND guideRepo.existsByEmail(req.email) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "អ៊ីមែលនេះមានក្នុងប្រព័ន្ធរួចហើយ")

    guide = mapper.toEntity(req)
    SET guide.uuid      = randomUUID()
    SET guide.code      = generateSequentialCode("GD", guideRepo.countByIsDeletedFalse())
    SET guide.status    = ACTIVE
    SET guide.isDeleted = false

    RETURN mapper.toResponse(guideRepo.save(guide))

END FUNCTION


FUNCTION findAvailable(startDate, endDate) -> List<GuideResponse>:

    IF startDate IS NULL OR endDate IS NULL THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ត្រូវបញ្ជាក់ startDate និង endDate")

    IF endDate IS BEFORE startDate THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate ត្រូវក្រោយ startDate")

    guides = guideRepo.findAvailableGuides(startDate, endDate)
    RETURN guides MAP mapper::toResponse

END FUNCTION


FUNCTION changeStatus(uuid, req) -> GuideResponse:

    guide = guideRepo.findByUuidAndIsDeletedFalse(uuid)
            ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញមគ្គុទ្ទេសក៍ uuid = " + uuid)

    IF guide.status == req.status THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "មគ្គុទ្ទេសក៍នេះស្ថិតក្នុងស្ថានភាព " + req.status + " រួចហើយ")

    # វិន័យ៖ ដាក់ INACTIVE មិនបានបើនៅមានកាលវិភាគអនាគត
    IF req.status == INACTIVE THEN
        upcoming = scheduleRepo.countUpcomingByGuide(guide.id, today())
        IF upcoming > 0 THEN
            THROW ResponseStatusException(HttpStatus.CONFLICT, "នៅមានកាលវិភាគអនាគត " + upcoming + " ត្រូវប្តូរមគ្គុទ្ទេសក៍សិន")

    SET guide.status = req.status
    RETURN mapper.toResponse(guideRepo.save(guide))

END FUNCTION
```

## F3.6 Controller

```
CONTROLLER GuideController  base = "/api/v1/guides"

    POST   "/"                 201   @Valid CreateGuideRequest
    GET    "/"                 200   status(optional), page=0, size=10
    GET    "/{uuid}"           200
    GET    "/available"        200   startDate, endDate  (ISO yyyy-MM-dd)
    PATCH  "/{uuid}"           200   @Valid UpdateGuideRequest
    PATCH  "/{uuid}/status"    200   @Valid UpdateGuideStatusRequest
    DELETE "/{uuid}"           204
```

---

# F4 — Tour (កញ្ចប់ដំណើរកម្សាន្ត)

**គោលបំណង** : គ្រប់គ្រងកញ្ចប់ដំណើរ — ឈ្មោះ · តម្លៃ · រយៈពេល · ទីតាំង · រូបភាព។

## F4.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC4.1 | Admin បង្កើត Tour | `POST /api/v1/tours` | ADMIN |
| UC4.2 | ស្វែងរក Tour (filter + sort + page) | `GET /api/v1/tours` | សាធារណៈ |
| UC4.3 | មើលលម្អិត Tour | `GET /api/v1/tours/{uuid}` | សាធារណៈ |
| UC4.4 | Admin កែ Tour | `PATCH /api/v1/tours/{uuid}` | ADMIN |
| UC4.5 | Admin បិទ/បើកលក់ | `PATCH /api/v1/tours/{uuid}/publish` | ADMIN |
| UC4.6 | Admin លុប Tour | `DELETE /api/v1/tours/{uuid}` | ADMIN |
| UC4.7 | មើល Tour ពេញនិយម (top rated) | `GET /api/v1/tours/popular` | សាធារណៈ |

## F4.2 Entity

```
ENTITY Tour EXTENDS BaseEntity  -> table "tours"
    code           : String        @Column(unique, nullable=false, length=20)   # TR-0001
    title          : String        @Column(nullable=false, length=180)
    slug           : String        @Column(unique, nullable=false, length=200)
    description    : String        @Column(columnDefinition = "TEXT")
    itinerary      : String        @Column(columnDefinition = "TEXT")  # កម្មវិធីថ្ងៃទី១, ថ្ងៃទី២...
    included       : String        @Column(columnDefinition = "TEXT")  # អ្វីរួមបញ្ចូល
    excluded       : String        @Column(columnDefinition = "TEXT")  # អ្វីមិនរួមបញ្ចូល
    price          : BigDecimal    @Column(nullable=false, precision=12, scale=2)
    durationDays   : Integer       @Column(nullable=false)
    durationNights : Integer
    minGroupSize   : Integer       DEFAULT 1
    maxGroupSize   : Integer       @Column(nullable=false)
    difficulty     : Difficulty    @Enumerated(STRING)
    thumbnailUrl   : String        @Column(length=255)
    isPublished    : Boolean       @Column(nullable=false)  DEFAULT false
    averageRating  : Double        DEFAULT 0.0
    reviewCount    : Integer       DEFAULT 0

    category       : Category          @ManyToOne(LAZY) @JoinColumn("category_id")
    destinations   : Set<Destination>  @ManyToMany  @JoinTable("tour_destinations")
    images         : List<TourImage>   @OneToMany(mappedBy="tour", cascade=ALL, orphanRemoval)
    schedules      : List<TourSchedule> @OneToMany(mappedBy="tour")

ENTITY TourImage  -> table "tour_images"
    id         : Long    @Id @GeneratedValue
    tour       : Tour    @ManyToOne(LAZY)
    url        : String  @Column(nullable=false, length=255)
    caption    : String  @Column(length=180)
    sortOrder  : Integer

ENUM Difficulty : EASY, MODERATE, CHALLENGING
```

## F4.3 DTOs

```
DTO CreateTourRequest:
    title            : String       @NotBlank @Size(max=180)
    description      : String       @Size(max=5000)
    itinerary        : String       @Size(max=10000)
    included         : String       @Size(max=3000)
    excluded         : String       @Size(max=3000)
    price            : BigDecimal   @NotNull @DecimalMin("0.01")
    durationDays     : Integer      @NotNull @Min(1) @Max(90)
    durationNights   : Integer      @Min(0)  @Max(90)
    minGroupSize     : Integer      @Min(1)
    maxGroupSize     : Integer      @NotNull @Min(1) @Max(200)
    difficulty       : Difficulty   @NotNull
    thumbnailUrl     : String       @Size(max=255)
    categoryUuid     : String       @NotBlank
    destinationUuids : Set<String>  @NotEmpty
    images           : List<TourImageRequest>  @Valid @Size(max=10)

DTO TourImageRequest:
    url        : String   @NotBlank @Size(max=255)
    caption    : String   @Size(max=180)
    sortOrder  : Integer  @Min(0)

DTO TourImageResponse:
    url, caption, sortOrder

DTO TourFilter:                     # query params សម្រាប់ UC4.2
    keyword          : String
    categoryUuid     : String
    destinationUuid  : String
    minPrice         : BigDecimal
    maxPrice         : BigDecimal
    minDays          : Integer
    maxDays          : Integer
    difficulty       : Difficulty
    departureAfter   : LocalDate
    sortBy           : String       # price_asc | price_desc | rating | newest

DTO TourCardResponse:               # សម្រាប់បញ្ជី (ស្រាល)
    uuid, code, title, slug, thumbnailUrl
    price, durationDays, durationNights, difficulty
    categoryName, averageRating, reviewCount
    nextDepartureDate : LocalDate

DTO TourDetailResponse:             # សម្រាប់លម្អិត (ធ្ងន់)
    ... គ្រប់ field នៃ TourCardResponse
    description, itinerary, included, excluded
    minGroupSize, maxGroupSize, isPublished
    destinations      : List<DestinationResponse>
    images            : List<TourImageResponse>
    upcomingSchedules : List<ScheduleResponse>
    recentReviews     : List<ReviewResponse>     # ៥ ចុងក្រោយ
```

## F4.4 Repository

```
REPOSITORY TourRepository EXTENDS JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour>

    findByUuidAndIsDeletedFalse(uuid)   -> Optional<Tour>
    findBySlugAndIsDeletedFalse(slug)   -> Optional<Tour>
    existsBySlug(slug)                  -> boolean
    countByIsDeletedFalse()             -> long

    @Query findPopular(pageable) -> Page<Tour>
        JPQL: SELECT t FROM Tour t
             WHERE t.isDeleted = false AND t.isPublished = true AND t.reviewCount >= 3
             ORDER BY t.averageRating DESC, t.reviewCount DESC

    @Query recalculateRating(tourId) -> void
        JPQL: UPDATE Tour t SET
                 t.averageRating = (SELECT COALESCE(AVG(r.rating),0) FROM Review r
                                    WHERE r.tour.id = :tourId AND r.isDeleted = false),
                 t.reviewCount   = (SELECT COUNT(r) FROM Review r
                                    WHERE r.tour.id = :tourId AND r.isDeleted = false)
             WHERE t.id = :tourId
```

## F4.5 Specification (សម្រាប់ UC4.2)

```
SPECIFICATION TourSpecs:

    isNotDeleted()              -> root.isDeleted = false
    isPublished()               -> root.isPublished = true
    titleOrDescLike(kw)         -> LOWER(root.title) LIKE %kw% OR LOWER(root.description) LIKE %kw%
    hasCategory(uuid)           -> root.category.uuid = uuid
    hasDestination(uuid)        -> JOIN root.destinations d WHERE d.uuid = uuid
    priceBetween(min, max)      -> root.price >= min AND root.price <= max
    daysBetween(min, max)       -> root.durationDays BETWEEN min AND max
    hasDifficulty(d)            -> root.difficulty = d
    hasScheduleAfter(date)      -> EXISTS (SELECT s FROM root.schedules s
                                           WHERE s.departureDate >= date
                                             AND s.status = 'OPEN')
```

## F4.6 Service

```
FUNCTION createNew(req) -> TourDetailResponse:

    # ១. Validate — វិន័យឆ្លង field
    IF req.durationNights IS NOT NULL AND req.durationNights > req.durationDays THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ចំនួនយប់មិនអាចលើសចំនួនថ្ងៃ")

    IF req.minGroupSize IS NOT NULL AND req.minGroupSize > req.maxGroupSize THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "minGroupSize មិនអាចលើស maxGroupSize")

    # ២. Load ធនធានពាក់ព័ន្ធ
    category = categoryRepo.findByUuidAndIsDeletedFalse(req.categoryUuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញប្រភេទ uuid = " + req.categoryUuid)

    destinations = destinationRepo.findAllByUuidIn(req.destinationUuids)

    IF destinations.size != req.destinationUuids.size THEN
        missing = req.destinationUuids MINUS (destinations MAP uuid)
        THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញទីតាំង៖ " + missing)

    # ៥. Build
    tour = mapper.toEntity(req)
    SET tour.uuid          = randomUUID()
    SET tour.code          = generateSequentialCode("TR", tourRepo.countByIsDeletedFalse())
    SET tour.slug          = generateUniqueSlug(req.title)
    SET tour.category      = category
    SET tour.destinations  = destinations
    SET tour.isPublished   = false        # បង្កើតជា draft ជានិច្ច
    SET tour.averageRating = 0.0
    SET tour.reviewCount   = 0
    SET tour.isDeleted     = false

    FOR EACH imgReq IN req.images DO
        image = NEW TourImage(url=imgReq.url, caption=imgReq.caption, sortOrder=imgReq.sortOrder)
        SET image.tour = tour
        ADD image TO tour.images

    # ៦. Save + ៨. Return
    RETURN mapper.toDetailResponse(tourRepo.save(tour))

END FUNCTION


FUNCTION search(filter, page, size) -> PageResponse<TourCardResponse>:

    # ១. ផ្សំលក្ខខណ្ឌជាបណ្តើរៗ
    spec = Specification.where(isNotDeleted()).and(isPublished())

    IF filter.keyword IS NOT EMPTY THEN
        spec = spec.and(titleOrDescLike(toLowerCase(filter.keyword)))

    IF filter.categoryUuid IS NOT NULL THEN
        spec = spec.and(hasCategory(filter.categoryUuid))

    IF filter.destinationUuid IS NOT NULL THEN
        spec = spec.and(hasDestination(filter.destinationUuid))

    IF filter.minPrice IS NOT NULL OR filter.maxPrice IS NOT NULL THEN
        IF filter.minPrice IS NOT NULL AND filter.maxPrice IS NOT NULL
           AND filter.minPrice > filter.maxPrice THEN
            THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice មិនអាចលើស maxPrice")
        spec = spec.and(priceBetween(filter.minPrice, filter.maxPrice))

    IF filter.minDays IS NOT NULL OR filter.maxDays IS NOT NULL THEN
        spec = spec.and(daysBetween(filter.minDays, filter.maxDays))

    IF filter.difficulty IS NOT NULL THEN
        spec = spec.and(hasDifficulty(filter.difficulty))

    IF filter.departureAfter IS NOT NULL THEN
        spec = spec.and(hasScheduleAfter(filter.departureAfter))

    # ២. រៀបលំដាប់
    sort = SWITCH filter.sortBy:
        CASE "price_asc"   -> Sort.by("price").ascending()
        CASE "price_desc"  -> Sort.by("price").descending()
        CASE "rating"      -> Sort.by("averageRating").descending()
        DEFAULT            -> Sort.by("createdAt").descending()

    # ៣. ប្រតិបត្តិ
    result = tourRepo.findAll(spec, PageRequest.of(page, size, sort))

    RETURN PageResponse.from(result MAP mapper::toCardResponse)

END FUNCTION


FUNCTION findByUuid(uuid) -> TourDetailResponse:

    tour = tourRepo.findByUuidAndIsDeletedFalse(uuid)
           ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Tour uuid = " + uuid)

    response = mapper.toDetailResponse(tour)
    SET response.upcomingSchedules = scheduleRepo.findOpenSchedulesByTour(tour.id, today())
                                     MAP scheduleMapper::toResponse
    SET response.recentReviews     = reviewRepo.findTop5ByTour(tour.id)
                                     MAP reviewMapper::toResponse
    RETURN response

END FUNCTION


FUNCTION publish(uuid, shouldPublish) -> TourDetailResponse:

    tour = tourRepo.findByUuidAndIsDeletedFalse(uuid)
           ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Tour uuid = " + uuid)

    IF shouldPublish == true THEN
        # វិន័យ៖ បើកលក់បានលុះត្រាតែពេញលក្ខខណ្ឌ
        IF tour.description IS EMPTY THEN
            THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ត្រូវបំពេញការពិពណ៌នាមុនបើកលក់")
        IF tour.thumbnailUrl IS EMPTY THEN
            THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ត្រូវដាក់រូបភាពគម្របមុនបើកលក់")
        IF tour.destinations IS EMPTY THEN
            THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ត្រូវមានទីតាំងយ៉ាងតិច ១")

        openSchedules = scheduleRepo.countOpenSchedules(tour.id, today())
        IF openSchedules == 0 THEN
            THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ត្រូវមានកាលវិភាគចេញដំណើរយ៉ាងតិច ១ មុនបើកលក់")

    SET tour.isPublished = shouldPublish
    RETURN mapper.toDetailResponse(tourRepo.save(tour))

END FUNCTION


FUNCTION deleteByUuid(uuid) -> void:

    tour = tourRepo.findByUuidAndIsDeletedFalse(uuid)
           ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Tour uuid = " + uuid)

    activeBookings = bookingRepo.countActiveBookingsByTour(tour.id)
    IF activeBookings > 0 THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "មិនអាចលុបបានទេ ព្រោះនៅមានការកក់សកម្ម " + activeBookings)

    SET tour.isPublished = false
    SET tour.isDeleted   = true
    tourRepo.save(tour)

END FUNCTION


FUNCTION findPopular(limit) -> List<TourCardResponse>:
    result = tourRepo.findPopular(PageRequest.of(0, limit ORELSE 10))
    RETURN result.content MAP mapper::toCardResponse
END FUNCTION
```

## F4.7 Controller

```
CONTROLLER TourController  base = "/api/v1/tours"

    POST   "/"                  201   @Valid CreateTourRequest
    GET    "/"                  200   TourFilter (query params), page=0, size=12
    GET    "/popular"           200   limit=10
    GET    "/{uuid}"            200
    PATCH  "/{uuid}"            200   @Valid UpdateTourRequest
    PATCH  "/{uuid}/publish"    200   body { published : boolean }
    DELETE "/{uuid}"            204
```

---

# F5 — Schedule (កាលវិភាគចេញដំណើរ)

**គោលបំណង** : កំណត់ថ្ងៃចេញដំណើរជាក់លាក់ · ចំនួនកៅអី · មគ្គុទ្ទេសក៍ · តម្លៃពិសេស។

## F5.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC5.1 | Admin បង្កើតកាលវិភាគ | `POST /api/v1/schedules` | ADMIN |
| UC5.2 | មើលកាលវិភាគរបស់ Tour មួយ | `GET /api/v1/tours/{tourUuid}/schedules` | សាធារណៈ |
| UC5.3 | មើលកាលវិភាគមួយ + កៅអីនៅសល់ | `GET /api/v1/schedules/{uuid}` | សាធារណៈ |
| UC5.4 | Admin ចាត់ចែងមគ្គុទ្ទេសក៍ | `PATCH /api/v1/schedules/{uuid}/guide` | ADMIN |
| UC5.5 | Admin បោះបង់កាលវិភាគ | `PATCH /api/v1/schedules/{uuid}/cancel` | ADMIN |
| UC5.6 | Admin កែកាលវិភាគ | `PATCH /api/v1/schedules/{uuid}` | ADMIN |

## F5.2 Entity

```
ENTITY TourSchedule EXTENDS BaseEntity  -> table "tour_schedules"
    code            : String          @Column(unique, nullable=false, length=25)  # SC-20260912-01
    tour            : Tour            @ManyToOne(LAZY, optional=false)
    guide           : Guide           @ManyToOne(LAZY)                # អាច null ដំបូង
    departureDate   : LocalDate       @Column(nullable=false)
    returnDate      : LocalDate       @Column(nullable=false)
    departureTime   : LocalTime
    meetingPoint    : String          @Column(length=255)
    capacity        : Integer         @Column(nullable=false)
    priceOverride   : BigDecimal      @Column(precision=12, scale=2)  # null = ប្រើតម្លៃ Tour
    status          : ScheduleStatus  @Enumerated(STRING)
    cancelReason    : String          @Column(length=500)
    bookings        : List<Booking>   @OneToMany(mappedBy="schedule")

ENUM ScheduleStatus : OPEN, FULL, CLOSED, DEPARTED, COMPLETED, CANCELLED
```

**ដ្យាក្រាមស្ថានភាព**

```
OPEN ──(កៅអីអស់)──> FULL ──(មានលុបចោល)──> OPEN
 │                    │
 │                    └──(ដល់ថ្ងៃចេញ)──> DEPARTED ──> COMPLETED
 ├──(admin បិទ)──> CLOSED
 └──(admin បោះបង់)──> CANCELLED   [ស្ថានភាពចុងក្រោយ]
```

## F5.3 DTOs

```
DTO CreateScheduleRequest:
    tourUuid       : String      @NotBlank
    guideUuid      : String                      # optional
    departureDate  : LocalDate   @NotNull @FutureOrPresent
    returnDate     : LocalDate   @NotNull @Future
    departureTime  : LocalTime
    meetingPoint   : String      @Size(max=255)
    capacity       : Integer     @NotNull @Min(1) @Max(200)
    priceOverride  : BigDecimal  @DecimalMin("0.01")

DTO AssignGuideRequest:
    guideUuid : String  @NotBlank

DTO CancelScheduleRequest:
    reason : String  @NotBlank @Size(max=500)

DTO ScheduleResponse:
    uuid, code
    tourUuid, tourTitle
    guideUuid, guideName
    departureDate, returnDate, departureTime, meetingPoint
    capacity
    bookedSeats     : int
    availableSeats  : int
    effectivePrice  : BigDecimal     # priceOverride ORELSE tour.price
    status
```

## F5.4 Repository

```
REPOSITORY ScheduleRepository EXTENDS JpaRepository<TourSchedule, Long>

    findByUuidAndIsDeletedFalse(uuid) -> Optional<TourSchedule>
    findByStatusInAndDepartureDateBefore(statuses, date) -> List<TourSchedule>
    findByStatusAndReturnDateBefore(status, date)        -> List<TourSchedule>

    @Query findByUuidForUpdate(uuid) -> Optional<TourSchedule>
        # @Lock(PESSIMISTIC_WRITE) — ចាក់សោជួរពេលកក់ ដើម្បីការពារ race condition
        JPQL: SELECT s FROM TourSchedule s WHERE s.uuid = :uuid AND s.isDeleted = false

    @Query findOpenSchedulesByTour(tourId, fromDate) -> List<TourSchedule>
        JPQL: SELECT s FROM TourSchedule s
             WHERE s.tour.id = :tourId AND s.isDeleted = false
               AND s.departureDate >= :fromDate
               AND s.status IN ('OPEN','FULL')
             ORDER BY s.departureDate ASC

    @Query countOpenSchedules(tourId, fromDate) -> long

    @Query countUpcomingByGuide(guideId, fromDate) -> long
        JPQL: SELECT COUNT(s) FROM TourSchedule s
             WHERE s.guide.id = :guideId
               AND s.departureDate >= :fromDate
               AND s.status NOT IN ('CANCELLED','COMPLETED')

    @Query hasGuideConflict(guideId, start, end, excludeScheduleId) -> boolean
        JPQL: SELECT COUNT(s) > 0 FROM TourSchedule s
             WHERE s.guide.id = :guideId
               AND s.status <> 'CANCELLED'
               AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
               AND s.departureDate <= :end
               AND s.returnDate    >= :start

    @Query findDepartingToday(date) -> List<TourSchedule>      # សម្រាប់ scheduled job
```

## F5.5 Service

```
FUNCTION createNew(req) -> ScheduleResponse:

    # ១. Validate កាលបរិច្ឆេទ
    IF req.returnDate IS BEFORE req.departureDate THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ថ្ងៃត្រឡប់ត្រូវក្រោយ ឬស្មើថ្ងៃចេញដំណើរ")

    IF req.departureDate IS BEFORE today() THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ថ្ងៃចេញដំណើរមិនអាចជាអតីតកាល")

    # ២. Load
    tour = tourRepo.findByUuidAndIsDeletedFalse(req.tourUuid)
           ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Tour uuid = " + req.tourUuid)

    # ៣. Check Rules
    actualDays = daysBetween(req.departureDate, req.returnDate) + 1
    IF actualDays != tour.durationDays THEN
        THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, 
            "រយៈពេលមិនត្រូវគ្នា — Tour នេះមាន " + tour.durationDays + " ថ្ងៃ តែកាលវិភាគមាន " + actualDays)

    IF req.capacity > tour.maxGroupSize THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "capacity មិនអាចលើស maxGroupSize របស់ Tour (" + tour.maxGroupSize + ")")

    IF req.capacity < tour.minGroupSize THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "capacity មិនអាចតិចជាង minGroupSize របស់ Tour")

    guide = NULL
    IF req.guideUuid IS NOT NULL THEN
        guide = guideRepo.findByUuidAndIsDeletedFalse(req.guideUuid)
                ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញមគ្គុទ្ទេសក៍")

        IF guide.status != ACTIVE THEN
            THROW ResponseStatusException(HttpStatus.CONFLICT, "មគ្គុទ្ទេសក៍នេះមិនសកម្ម")

        IF scheduleRepo.hasGuideConflict(guide.id, req.departureDate, req.returnDate, NULL) THEN
            THROW ResponseStatusException(HttpStatus.CONFLICT, "មគ្គុទ្ទេសក៍នេះមានកាលវិភាគជាន់គ្នាក្នុងចន្លោះថ្ងៃនេះ")

    # ៥. Build
    schedule = NEW TourSchedule
    SET schedule.uuid          = randomUUID()
    SET schedule.code          = generateDateCode("SC", req.departureDate)
    SET schedule.tour          = tour
    SET schedule.guide         = guide
    SET schedule.departureDate = req.departureDate
    SET schedule.returnDate    = req.returnDate
    SET schedule.departureTime = req.departureTime
    SET schedule.meetingPoint  = req.meetingPoint
    SET schedule.capacity      = req.capacity
    SET schedule.priceOverride = req.priceOverride
    SET schedule.status        = OPEN
    SET schedule.isDeleted     = false

    # ៦. Save + ៨. Return
    RETURN toResponseWithSeats(scheduleRepo.save(schedule))

END FUNCTION


FUNCTION toResponseWithSeats(schedule) -> ScheduleResponse:
    response = mapper.toResponse(schedule)
    SET booked = bookingRepo.countOccupiedSeats(schedule.id)
    SET response.bookedSeats     = booked
    SET response.availableSeats  = schedule.capacity - booked
    SET response.effectivePrice  = schedule.priceOverride ORELSE schedule.tour.price
    RETURN response
END FUNCTION


FUNCTION assignGuide(uuid, req) -> ScheduleResponse:

    schedule = scheduleRepo.findByUuidAndIsDeletedFalse(uuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញកាលវិភាគ uuid = " + uuid)

    IF schedule.status IN (CANCELLED, COMPLETED, DEPARTED) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "មិនអាចប្តូរមគ្គុទ្ទេសក៍លើកាលវិភាគស្ថានភាព " + schedule.status)

    guide = guideRepo.findByUuidAndIsDeletedFalse(req.guideUuid)
            ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញមគ្គុទ្ទេសក៍")

    IF guide.status != ACTIVE THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "មគ្គុទ្ទេសក៍នេះមិនសកម្ម")

    IF scheduleRepo.hasGuideConflict(guide.id, schedule.departureDate,
                                     schedule.returnDate, schedule.id) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "មគ្គុទ្ទេសក៍នេះមានកាលវិភាគជាន់គ្នា")

    SET schedule.guide = guide
    RETURN toResponseWithSeats(scheduleRepo.save(schedule))

END FUNCTION


FUNCTION cancel(uuid, req) -> ScheduleResponse:      # @Transactional

    schedule = scheduleRepo.findByUuidAndIsDeletedFalse(uuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញកាលវិភាគ uuid = " + uuid)

    IF schedule.status == CANCELLED THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "កាលវិភាគនេះត្រូវបានបោះបង់រួចហើយ")

    IF schedule.status IN (DEPARTED, COMPLETED) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ដំណើរបានចេញ/បញ្ចប់ហើយ មិនអាចបោះបង់បានទេ")

    SET schedule.status       = CANCELLED
    SET schedule.cancelReason = req.reason
    scheduleRepo.save(schedule)

    # ៧. Side Effects — លុបចោលការកក់ទាំងអស់ + សងប្រាក់វិញ
    affected = bookingRepo.findActiveBySchedule(schedule.id)
    FOR EACH booking IN affected DO
        SET booking.status       = CANCELLED
        SET booking.cancelledAt  = now()
        SET booking.cancelReason = "កាលវិភាគត្រូវបានបោះបង់៖ " + req.reason
        bookingRepo.save(booking)

        CALL paymentService.refundFull(booking, "កាលវិភាគត្រូវបានបោះបង់")
        CALL notificationService.notifyScheduleCancelled(booking)

    RETURN toResponseWithSeats(schedule)

END FUNCTION


# ---- Scheduled Job (រត់ដោយស្វ័យប្រវត្តិរាល់ថ្ងៃម៉ោង ០១:០០) ----
FUNCTION refreshScheduleStatuses():                  # @Scheduled(cron = "0 0 1 * * *")

    # ក. កាលវិភាគដល់ថ្ងៃចេញ -> DEPARTED
    FOR EACH s IN scheduleRepo.findByStatusInAndDepartureDateBefore([OPEN, FULL], today()) DO
        SET s.status = DEPARTED
        scheduleRepo.save(s)

    # ខ. កាលវិភាគត្រឡប់រួច -> COMPLETED
    FOR EACH s IN scheduleRepo.findByStatusAndReturnDateBefore(DEPARTED, today()) DO
        SET s.status = COMPLETED
        scheduleRepo.save(s)

        FOR EACH b IN bookingRepo.findByScheduleAndStatus(s.id, CONFIRMED) DO
            SET b.status = COMPLETED
            bookingRepo.save(b)
            CALL notificationService.inviteToReview(b)

END FUNCTION
```

## F5.6 Controller

```
CONTROLLER ScheduleController  base = "/api/v1/schedules"

    POST   "/"                  201   @Valid CreateScheduleRequest
    GET    "/{uuid}"            200
    PATCH  "/{uuid}"            200   @Valid UpdateScheduleRequest
    PATCH  "/{uuid}/guide"      200   @Valid AssignGuideRequest
    PATCH  "/{uuid}/cancel"     200   @Valid CancelScheduleRequest

CONTROLLER TourScheduleController  base = "/api/v1/tours/{tourUuid}/schedules"

    GET    "/"                  200   fromDate(optional, default today)
           -> RETURN scheduleService.findByTour(tourUuid, fromDate)
```

---

# F6 — Customer (អតិថិជន)

**គោលបំណង** : ព័ត៌មានផ្ទាល់ខ្លួនអតិថិជន និងប្រវត្តិដំណើរកម្សាន្ត។

## F6.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC6.1 | ចុះឈ្មោះអតិថិជនថ្មី | `POST /api/v1/customers/register` | សាធារណៈ |
| UC6.2 | មើលប្រវត្តិរូបខ្លួនឯង | `GET /api/v1/customers/me` | CUSTOMER |
| UC6.3 | កែប្រវត្តិរូបខ្លួនឯង | `PATCH /api/v1/customers/me` | CUSTOMER |
| UC6.4 | Admin មើលបញ្ជីអតិថិជន | `GET /api/v1/customers` | ADMIN |
| UC6.5 | Admin មើលអតិថិជនម្នាក់ | `GET /api/v1/customers/{uuid}` | ADMIN |
| UC6.6 | Admin ផ្អាកគណនី | `PATCH /api/v1/customers/{uuid}/status` | ADMIN |

## F6.2 Entity

```
ENTITY Customer EXTENDS BaseEntity  -> table "customers"
    username     : String          @Column(unique, nullable=false, length=60)
    fullName     : String          @Column(nullable=false, length=120)
    email        : String          @Column(unique, nullable=false, length=120)
    phoneNumber  : String          @Column(unique, nullable=false, length=20)
    gender       : Gender          @Enumerated(STRING)
    dateOfBirth  : LocalDate
    nationality  : String          @Column(length=80)
    passportNo   : String          @Column(length=40)
    address      : String          @Column(length=255)
    avatarUrl    : String          @Column(length=255)
    status       : CustomerStatus  @Enumerated(STRING)
    bookings     : List<Booking>   @OneToMany(mappedBy="customer")
    reviews      : List<Review>    @OneToMany(mappedBy="customer")

ENUM CustomerStatus : ACTIVE, SUSPENDED, BLOCKED
```

## F6.3 DTOs

```
DTO RegisterCustomerRequest:
    username     : String     @NotBlank @Size(min=4, max=60) @Pattern("^[a-zA-Z0-9._-]+$")
    fullName     : String     @NotBlank @Size(max=120)
    email        : String     @NotBlank @Email
    phoneNumber  : String     @NotBlank @Pattern("^0[1-9][0-9]{7,8}$")
    gender       : Gender
    dateOfBirth  : LocalDate  @Past
    nationality  : String     @Size(max=80)

DTO PatchCustomerRequest:       # គ្រប់ field nullable; username និង email កែមិនបាន
    fullName, phoneNumber, gender, dateOfBirth
    nationality, passportNo, address, avatarUrl

DTO UpdateCustomerStatusRequest:
    status : CustomerStatus  @NotNull
    reason : String          @Size(max=255)

DTO CustomerResponse:
    uuid, username, fullName, email, phoneNumber
    gender, dateOfBirth, nationality, address, avatarUrl, status
    totalBookings    : long
    completedTours   : long
    memberSince      : LocalDate
```

## F6.4 Repository

```
REPOSITORY CustomerRepository EXTENDS JpaRepository<Customer, Long>

    findByUuidAndIsDeletedFalse(uuid)     -> Optional<Customer>
    findByUsernameAndIsDeletedFalse(u)    -> Optional<Customer>
    existsByUsername(username)            -> boolean
    existsByEmail(email)                  -> boolean
    existsByPhoneNumber(phone)            -> boolean
    findAllByIsDeletedFalse(pageable)     -> Page<Customer>

    @Query searchByKeyword(keyword, pageable) -> Page<Customer>
        JPQL: SELECT c FROM Customer c
             WHERE c.isDeleted = false
               AND (LOWER(c.fullName) LIKE %:keyword%
                 OR LOWER(c.email)    LIKE %:keyword%
                 OR c.phoneNumber     LIKE %:keyword%)
```

## F6.5 Service

```
FUNCTION register(req) -> CustomerResponse:

    # ៣. Check Rules — ស្ទួន ៣ ចំណុច
    IF customerRepo.existsByUsername(req.username) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ឈ្មោះអ្នកប្រើ '" + req.username + "' ត្រូវបានប្រើរួចហើយ")

    IF customerRepo.existsByEmail(req.email) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "អ៊ីមែលនេះត្រូវបានចុះឈ្មោះរួចហើយ")

    IF customerRepo.existsByPhoneNumber(req.phoneNumber) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "លេខទូរស័ព្ទនេះត្រូវបានចុះឈ្មោះរួចហើយ")

    # វិន័យអាយុ
    IF req.dateOfBirth IS NOT NULL THEN
        age = yearsBetween(req.dateOfBirth, today())
        IF age < 16 THEN
            THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "អ្នកប្រើត្រូវមានអាយុយ៉ាងតិច ១៦ ឆ្នាំ")

    # ៥. Build
    customer = mapper.toEntity(req)
    SET customer.uuid      = randomUUID()
    SET customer.status    = ACTIVE
    SET customer.isDeleted = false

    saved = customerRepo.save(customer)

    # ៧. Side Effects
    CALL notificationService.sendWelcomeEmail(saved)

    RETURN toResponseWithStats(saved)

END FUNCTION


FUNCTION toResponseWithStats(customer) -> CustomerResponse:
    response = mapper.toResponse(customer)
    SET response.totalBookings  = bookingRepo.countByCustomerId(customer.id)
    SET response.completedTours = bookingRepo.countByCustomerIdAndStatus(customer.id, COMPLETED)
    SET response.memberSince    = customer.createdAt.toLocalDate()
    RETURN response
END FUNCTION


FUNCTION findMe(username) -> CustomerResponse:
    customer = customerRepo.findByUsernameAndIsDeletedFalse(username)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញគណនីរបស់អ្នក")
    RETURN toResponseWithStats(customer)
END FUNCTION


FUNCTION updateMe(username, req) -> CustomerResponse:

    customer = customerRepo.findByUsernameAndIsDeletedFalse(username)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញគណនីរបស់អ្នក")

    IF customer.status != ACTIVE THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "គណនីរបស់អ្នកស្ថិតក្នុងស្ថានភាព " + customer.status)

    IF req.phoneNumber IS NOT NULL AND req.phoneNumber != customer.phoneNumber THEN
        IF customerRepo.existsByPhoneNumber(req.phoneNumber) THEN
            THROW ResponseStatusException(HttpStatus.CONFLICT, "លេខទូរស័ព្ទនេះត្រូវបានប្រើរួចហើយ")

    mapper.toEntity(req, customer)
    RETURN toResponseWithStats(customerRepo.save(customer))

END FUNCTION


FUNCTION changeStatus(uuid, req) -> CustomerResponse:

    customer = customerRepo.findByUuidAndIsDeletedFalse(uuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញអតិថិជន uuid = " + uuid)

    IF customer.status == req.status THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "គណនីនេះស្ថិតក្នុងស្ថានភាព " + req.status + " រួចហើយ")

    IF req.status IN (SUSPENDED, BLOCKED) THEN
        active = bookingRepo.countActiveByCustomer(customer.id)
        IF active > 0 THEN
            # មិនហាមទេ តែត្រូវដាក់ហេតុផល
            IF req.reason IS EMPTY THEN
                THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ត្រូវបញ្ជាក់ហេតុផល ព្រោះអតិថិជននេះមានការកក់សកម្ម " + active)

    SET customer.status = req.status
    saved = customerRepo.save(customer)

    CALL notificationService.notifyAccountStatusChanged(saved, req.reason)

    RETURN toResponseWithStats(saved)

END FUNCTION
```

## F6.6 Controller

```
CONTROLLER CustomerController  base = "/api/v1/customers"

    POST   "/register"         201   @Valid RegisterCustomerRequest
    GET    "/me"               200   Authentication auth
           -> RETURN service.findMe(auth.getName())
    PATCH  "/me"               200   @Valid PatchCustomerRequest, Authentication auth
    GET    "/"                 200   keyword(optional), page=0, size=10       [ADMIN]
    GET    "/{uuid}"           200                                            [ADMIN]
    PATCH  "/{uuid}/status"    200   @Valid UpdateCustomerStatusRequest       [ADMIN]
```

---

# F7 — Booking (ការកក់) ⭐ Feature ស្មុគស្មាញបំផុត

**គោលបំណង** : អតិថិជនកក់កៅអីលើកាលវិភាគ · គ្រប់គ្រងវដ្តជីវិតការកក់។

## F7.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC7.1 | អតិថិជនកក់ Tour | `POST /api/v1/bookings` | CUSTOMER |
| UC7.2 | មើលការកក់របស់ខ្លួន | `GET /api/v1/bookings/me` | CUSTOMER |
| UC7.3 | មើលលម្អិតការកក់មួយ | `GET /api/v1/bookings/{code}` | CUSTOMER/ADMIN |
| UC7.4 | អតិថិជនលុបចោលការកក់ | `PATCH /api/v1/bookings/{code}/cancel` | CUSTOMER |
| UC7.5 | Admin បញ្ជាក់ការកក់ | `PATCH /api/v1/bookings/{code}/confirm` | ADMIN |
| UC7.6 | Admin មើលការកក់ទាំងអស់ | `GET /api/v1/bookings` | ADMIN |
| UC7.7 | អតិថិជនកែចំនួនអ្នកដំណើរ | `PATCH /api/v1/bookings/{code}/passengers` | CUSTOMER |

## F7.2 Entity

```
ENTITY Booking EXTENDS BaseEntity  -> table "bookings"
    code            : String            @Column(unique, nullable=false, length=30)  # BK-20260912-0001
    customer        : Customer          @ManyToOne(LAZY, optional=false)
    schedule        : TourSchedule      @ManyToOne(LAZY, optional=false)
    numberOfPeople  : Integer           @Column(nullable=false)
    unitPrice       : BigDecimal        @Column(nullable=false, precision=12, scale=2)
    subTotal        : BigDecimal        @Column(nullable=false, precision=12, scale=2)
    discountAmount  : BigDecimal        @Column(precision=12, scale=2)  DEFAULT 0
    totalPrice      : BigDecimal        @Column(nullable=false, precision=12, scale=2)
    paidAmount      : BigDecimal        @Column(precision=12, scale=2)  DEFAULT 0
    status          : BookingStatus     @Enumerated(STRING)
    note            : String            @Column(length=500)
    cancelReason    : String            @Column(length=500)
    bookedAt        : LocalDateTime     @Column(nullable=false)
    confirmedAt     : LocalDateTime
    cancelledAt     : LocalDateTime
    passengers      : List<Passenger>   @OneToMany(mappedBy="booking", cascade=ALL, orphanRemoval)
    payments        : List<Payment>     @OneToMany(mappedBy="booking")
    review          : Review            @OneToOne(mappedBy="booking")

    @Version version : Long             # optimistic locking

ENTITY Passenger  -> table "passengers"
    id           : Long     @Id @GeneratedValue
    booking      : Booking  @ManyToOne(LAZY)
    fullName     : String   @Column(nullable=false, length=120)
    gender       : Gender   @Enumerated(STRING)
    dateOfBirth  : LocalDate
    passportNo   : String   @Column(length=40)
    isLead       : Boolean  DEFAULT false

ENUM BookingStatus : PENDING, CONFIRMED, CANCELLED, COMPLETED
```

**ដ្យាក្រាមស្ថានភាព**

```
        ┌──────────────────────────┐
        │                          ▼
   [កក់] ──> PENDING ──(admin confirm)──> CONFIRMED ──(ដំណើរបញ្ចប់)──> COMPLETED
                │                            │
                └──(លុបចោល)──> CANCELLED <───┘
                                (សងប្រាក់តាមវិន័យ)
```

## F7.3 វិន័យអាជីវកម្ម (Business Rules) — សំខាន់បំផុត

| # | វិន័យ | កំហុសបើល្មើស |
|---|---|---|
| BR1 | កាលវិភាគត្រូវស្ថិតក្នុងស្ថានភាព `OPEN` | 409 Conflict |
| BR2 | ថ្ងៃចេញដំណើរត្រូវនៅអនាគត | 409 Conflict |
| BR3 | កៅអីនៅសល់ ≥ ចំនួនអ្នកដំណើរ | 409 Conflict |
| BR4 | អតិថិជនត្រូវស្ថិតក្នុងស្ថានភាព `ACTIVE` | 403 Forbidden |
| BR5 | អតិថិជនម្នាក់កក់កាលវិភាគដដែលពីរដងមិនបាន (ពេលនៅ PENDING/CONFIRMED) | 409 Conflict |
| BR6 | ចំនួនអ្នកដំណើរត្រូវនៅចន្លោះ `minGroupSize` និង `maxGroupSize` | 400 Bad Request |
| BR7 | បញ្ជីអ្នកដំណើរ (passengers) ត្រូវមានចំនួនស្មើ `numberOfPeople` | 400 Bad Request |
| BR8 | លុបចោលបានតែមុន ៣ ថ្ងៃនៃថ្ងៃចេញដំណើរ | 409 Conflict |
| BR9 | បញ្ជាក់ (confirm) បានតែពេលបង់ប្រាក់គ្រប់ ឬយ៉ាងតិច ៥០% | 422 Unprocessable |

**វិន័យបញ្ចុះតម្លៃ (BR10)**

```
FUNCTION calculateDiscount(numberOfPeople, subTotal) -> BigDecimal:
    IF numberOfPeople >= 10 THEN RETURN subTotal * 0.15      # ក្រុមធំ ១៥%
    IF numberOfPeople >= 5  THEN RETURN subTotal * 0.10      # ក្រុមមធ្យម ១០%
    IF numberOfPeople >= 3  THEN RETURN subTotal * 0.05      # ក្រុមតូច ៥%
    RETURN 0
END FUNCTION
```

## F7.4 DTOs

```
DTO CreateBookingRequest:
    scheduleUuid    : String              @NotBlank
    numberOfPeople  : Integer             @NotNull @Min(1) @Max(20)
    note            : String              @Size(max=500)
    passengers      : List<PassengerRequest>  @NotEmpty @Valid

DTO PassengerRequest:
    fullName     : String     @NotBlank @Size(max=120)
    gender       : Gender     @NotNull
    dateOfBirth  : LocalDate  @Past
    passportNo   : String     @Size(max=40)
    isLead       : Boolean

DTO CancelBookingRequest:
    reason : String  @NotBlank @Size(max=500)

DTO UpdatePassengersRequest:
    numberOfPeople : Integer                  @NotNull @Min(1)
    passengers     : List<PassengerRequest>   @NotEmpty @Valid

DTO BookingResponse:
    code, status
    tourTitle, tourUuid
    scheduleUuid, departureDate, returnDate, meetingPoint
    numberOfPeople
    unitPrice, subTotal, discountAmount, totalPrice
    paidAmount, remainingAmount
    bookedAt, confirmedAt, cancelledAt
    customerName        # បង្ហាញតែសម្រាប់ ADMIN

DTO BookingDetailResponse EXTENDS BookingResponse:
    note, cancelReason
    guideName
    passengers  : List<PassengerResponse>
    payments    : List<PaymentResponse>
```

## F7.5 Repository

```
REPOSITORY BookingRepository EXTENDS JpaRepository<Booking, Long>

    findByCodeAndIsDeletedFalse(code)  -> Optional<Booking>
    existsByCode(code)                 -> boolean
    countByCustomerId(customerId)                        -> long
    countByCustomerIdAndStatus(customerId, status)       -> long
    findAllByCustomerUsernameAndIsDeletedFalse(u, pageable) -> Page<Booking>
    findAllByCustomerUsernameAndStatus(u, status, pageable) -> Page<Booking>
    findByScheduleIdAndStatus(scheduleId, status)        -> List<Booking>

    @Query countOccupiedSeats(scheduleId) -> int
        JPQL: SELECT COALESCE(SUM(b.numberOfPeople), 0) FROM Booking b
             WHERE b.schedule.id = :scheduleId
               AND b.status IN ('PENDING','CONFIRMED','COMPLETED')
               AND b.isDeleted = false

    @Query existsActiveBooking(customerId, scheduleId) -> boolean
        JPQL: SELECT COUNT(b) > 0 FROM Booking b
             WHERE b.customer.id = :customerId
               AND b.schedule.id = :scheduleId
               AND b.status IN ('PENDING','CONFIRMED')
               AND b.isDeleted = false

    @Query countActiveBookingsByTour(tourId) -> long
    @Query countActiveByCustomer(customerId) -> long
    @Query findActiveBySchedule(scheduleId)  -> List<Booking>

    @Query countTodayBookings() -> long                # សម្រាប់បង្កើត code
        JPQL: SELECT COUNT(b) FROM Booking b WHERE DATE(b.bookedAt) = CURRENT_DATE

    @Query findExpiredPending(cutoffTime) -> List<Booking>    # សម្រាប់ auto-cancel
        JPQL: SELECT b FROM Booking b
             WHERE b.status = 'PENDING' AND b.bookedAt < :cutoffTime
```

## F7.6 Service — UC7.1 កក់ Tour (មុខងារស្នូល)

```
FUNCTION bookTour(req, username) -> BookingDetailResponse:      # @Transactional

    # ═══ ១. VALIDATE ═══
    IF req.passengers.size != req.numberOfPeople THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ចំនួនអ្នកដំណើរក្នុងបញ្ជី (" + req.passengers.size
                       + ") មិនត្រូវនឹង numberOfPeople (" + req.numberOfPeople + ")")     # BR7

    leadCount = COUNT p IN req.passengers WHERE p.isLead == true
    IF leadCount != 1 THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ត្រូវកំណត់អ្នកដំណើរមេ (isLead) ឲ្យបានតែម្នាក់")

    # ═══ ២. LOAD ═══
    customer = customerRepo.findByUsernameAndIsDeletedFalse(username)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញគណនីរបស់អ្នក")

    # ចាក់សោជួរ (pessimistic lock) ដើម្បីការពារ race condition ពេលកក់ព្រមគ្នា
    schedule = scheduleRepo.findByUuidForUpdate(req.scheduleUuid)
               ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញកាលវិភាគ uuid = " + req.scheduleUuid)

    tour = schedule.tour

    # ═══ ៣. CHECK RULES ═══
    IF customer.status != ACTIVE THEN                                                    # BR4
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "គណនីរបស់អ្នកមិនអាចធ្វើការកក់បានទេ (" + customer.status + ")")

    IF schedule.status != OPEN THEN                                                      # BR1
        THROW ResponseStatusException(HttpStatus.CONFLICT, "កាលវិភាគនេះមិនបើកទទួលការកក់ទេ (" + schedule.status + ")")

    IF schedule.departureDate IS NOT AFTER today() THEN                                  # BR2
        THROW ResponseStatusException(HttpStatus.CONFLICT, "កាលវិភាគនេះផុតកំណត់ទទួលការកក់ហើយ")

    IF bookingRepo.existsActiveBooking(customer.id, schedule.id) THEN                    # BR5
        THROW ResponseStatusException(HttpStatus.CONFLICT, "អ្នកបានកក់កាលវិភាគនេះរួចហើយ")

    IF req.numberOfPeople < tour.minGroupSize THEN                                       # BR6
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "Tour នេះទាមទារយ៉ាងតិច " + tour.minGroupSize + " នាក់")

    IF req.numberOfPeople > tour.maxGroupSize THEN                                       # BR6
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "Tour នេះទទួលបានច្រើនបំផុត " + tour.maxGroupSize + " នាក់")

    bookedSeats    = bookingRepo.countOccupiedSeats(schedule.id)
    availableSeats = schedule.capacity - bookedSeats

    IF req.numberOfPeople > availableSeats THEN                                          # BR3
        IF availableSeats == 0 THEN
            THROW ResponseStatusException(HttpStatus.CONFLICT, "កាលវិភាគនេះពេញហើយ")
        ELSE
            THROW ResponseStatusException(HttpStatus.CONFLICT, "នៅសល់តែ " + availableSeats + " កៅអីប៉ុណ្ណោះ")

    # ═══ ៤. CALCULATE ═══
    unitPrice  = schedule.priceOverride ORELSE tour.price
    subTotal   = unitPrice * req.numberOfPeople
    discount   = calculateDiscount(req.numberOfPeople, subTotal)                        # BR10
    totalPrice = subTotal - discount

    # ═══ ៥. BUILD ═══
    booking = NEW Booking
    SET booking.uuid            = randomUUID()
    SET booking.code            = generateBookingCode()      # BK-20260912-0001
    SET booking.customer        = customer
    SET booking.schedule        = schedule
    SET booking.numberOfPeople  = req.numberOfPeople
    SET booking.unitPrice       = unitPrice
    SET booking.subTotal        = subTotal
    SET booking.discountAmount  = discount
    SET booking.totalPrice      = totalPrice
    SET booking.paidAmount      = 0
    SET booking.status          = PENDING
    SET booking.note            = req.note
    SET booking.bookedAt        = now()
    SET booking.isDeleted       = false

    FOR EACH p IN req.passengers DO
        passenger = mapper.toPassenger(p)
        SET passenger.booking = booking
        ADD passenger TO booking.passengers

    # ═══ ៦. SAVE ═══
    saved = bookingRepo.save(booking)

    # ═══ ៧. SIDE EFFECTS ═══
    # បើពេញកៅអី -> ប្តូរស្ថានភាពកាលវិភាគ
    IF (bookedSeats + req.numberOfPeople) >= schedule.capacity THEN
        SET schedule.status = FULL
        scheduleRepo.save(schedule)

    CALL notificationService.sendBookingCreated(saved)

    # ═══ ៨. RETURN ═══
    RETURN mapper.toDetailResponse(saved)

END FUNCTION


FUNCTION generateBookingCode() -> String:
    datePart = format(today(), "yyyyMMdd")
    SET seq  = bookingRepo.countTodayBookings() + 1
    SET code = "BK-" + datePart + "-" + padLeft(seq, 4, '0')

    # ការពារការប៉ះទង្គិច
    WHILE bookingRepo.existsByCode(code) DO
        SET seq  = seq + 1
        SET code = "BK-" + datePart + "-" + padLeft(seq, 4, '0')

    RETURN code
END FUNCTION
```

## F7.7 Service — UC7.4 លុបចោលការកក់

```
FUNCTION cancel(code, req, username) -> BookingResponse:        # @Transactional

    booking = bookingRepo.findByCodeAndIsDeletedFalse(code)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការកក់លេខ " + code)

    # វិន័យសិទ្ធិ
    IF booking.customer.username != username THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិលើការកក់នេះទេ")

    # វិន័យស្ថានភាព
    IF booking.status == CANCELLED THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ការកក់នេះត្រូវបានលុបចោលរួចហើយ")

    IF booking.status == COMPLETED THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ដំណើរបានបញ្ចប់ហើយ មិនអាចលុបចោលបានទេ")

    # វិន័យពេលវេលា                                                                  # BR8
    daysLeft = daysBetween(today(), booking.schedule.departureDate)
    IF daysLeft < 3 THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ត្រូវលុបចោលយ៉ាងតិច ៣ ថ្ងៃមុនចេញដំណើរ (នៅសល់ " + daysLeft + " ថ្ងៃ)")

    # គណនាចំនួនប្រាក់សងវិញ
    refundRate  = calculateRefundRate(daysLeft)
    refundAmount = booking.paidAmount * refundRate

    SET booking.status       = CANCELLED
    SET booking.cancelReason = req.reason
    SET booking.cancelledAt  = now()
    bookingRepo.save(booking)

    # Side Effects
    IF refundAmount > 0 THEN
        CALL paymentService.createRefund(booking, refundAmount, req.reason)

    # បើកកៅអីវិញ
    schedule = booking.schedule
    IF schedule.status == FULL THEN
        SET schedule.status = OPEN
        scheduleRepo.save(schedule)

    CALL notificationService.sendBookingCancelled(booking, refundAmount)

    RETURN mapper.toResponse(booking)

END FUNCTION


FUNCTION calculateRefundRate(daysLeft) -> BigDecimal:
    IF daysLeft >= 30 THEN RETURN 1.00      # សងវិញ ១០០%
    IF daysLeft >= 14 THEN RETURN 0.75      # សងវិញ ៧៥%
    IF daysLeft >= 7  THEN RETURN 0.50      # សងវិញ ៥០%
    IF daysLeft >= 3  THEN RETURN 0.25      # សងវិញ ២៥%
    RETURN 0
END FUNCTION
```

## F7.8 Service — មុខងារផ្សេងទៀត

```
FUNCTION confirm(code) -> BookingResponse:                      # @Transactional [ADMIN]

    booking = bookingRepo.findByCodeAndIsDeletedFalse(code)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការកក់លេខ " + code)

    IF booking.status != PENDING THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "បញ្ជាក់បានតែការកក់ស្ថានភាព PENDING (បច្ចុប្បន្ន " + booking.status + ")")

    # វិន័យទូទាត់                                                                    # BR9
    minimumRequired = booking.totalPrice * 0.50
    IF booking.paidAmount < minimumRequired THEN
        THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, 
            "ត្រូវបង់យ៉ាងតិច ៥០% (" + minimumRequired + ") មុនបញ្ជាក់ — បង់រួច " + booking.paidAmount)

    SET booking.status      = CONFIRMED
    SET booking.confirmedAt = now()
    bookingRepo.save(booking)

    CALL notificationService.sendBookingConfirmed(booking)

    RETURN mapper.toResponse(booking)

END FUNCTION


FUNCTION findMyBookings(username, status, page, size) -> PageResponse<BookingResponse>:
    pageable = buildPageable(page, size, "bookedAt", DESC)

    IF status IS NOT NULL THEN
        result = bookingRepo.findAllByCustomerUsernameAndStatus(username, status, pageable)
    ELSE
        result = bookingRepo.findAllByCustomerUsernameAndIsDeletedFalse(username, pageable)

    RETURN PageResponse.from(result MAP mapper::toResponse)
END FUNCTION


FUNCTION findByCode(code, username, isAdmin) -> BookingDetailResponse:

    booking = bookingRepo.findByCodeAndIsDeletedFalse(code)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការកក់លេខ " + code)

    IF isAdmin == false AND booking.customer.username != username THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិមើលការកក់នេះទេ")

    RETURN mapper.toDetailResponse(booking)

END FUNCTION


FUNCTION updatePassengers(code, req, username) -> BookingDetailResponse:   # @Transactional

    booking = bookingRepo.findByCodeAndIsDeletedFalse(code)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការកក់លេខ " + code)

    IF booking.customer.username != username THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិលើការកក់នេះទេ")

    IF booking.status != PENDING THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "កែចំនួនអ្នកដំណើរបានតែពេលការកក់នៅ PENDING")

    IF req.passengers.size != req.numberOfPeople THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ចំនួនអ្នកដំណើរក្នុងបញ្ជីមិនត្រូវគ្នា")

    # ពិនិត្យកៅអី — កាត់ចេញនូវការកក់បច្ចុប្បន្ន
    schedule  = booking.schedule
    occupied  = bookingRepo.countOccupiedSeats(schedule.id) - booking.numberOfPeople
    available = schedule.capacity - occupied

    IF req.numberOfPeople > available THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "នៅសល់តែ " + available + " កៅអី")

    # គណនាតម្លៃឡើងវិញ
    SET booking.numberOfPeople = req.numberOfPeople
    SET booking.subTotal       = booking.unitPrice * req.numberOfPeople
    SET booking.discountAmount = calculateDiscount(req.numberOfPeople, booking.subTotal)
    SET booking.totalPrice     = booking.subTotal - booking.discountAmount

    # បើបង់លើសរួច -> ត្រូវសងវិញ
    IF booking.paidAmount > booking.totalPrice THEN
        overpaid = booking.paidAmount - booking.totalPrice
        CALL paymentService.createRefund(booking, overpaid, "កាត់បន្ថយចំនួនអ្នកដំណើរ")

    # ជំនួសបញ្ជីអ្នកដំណើរទាំងស្រុង
    CLEAR booking.passengers                      # orphanRemoval នឹងលុបចាស់
    FOR EACH p IN req.passengers DO
        passenger = mapper.toPassenger(p)
        SET passenger.booking = booking
        ADD passenger TO booking.passengers

    RETURN mapper.toDetailResponse(bookingRepo.save(booking))

END FUNCTION


# ---- Scheduled Job — លុបចោលការកក់ដែលមិនបង់ប្រាក់ក្នុង ២៤ ម៉ោង ----
FUNCTION autoCancelExpiredPending():             # @Scheduled(cron = "0 */30 * * * *")

    cutoff = now() MINUS 24 hours

    FOR EACH booking IN bookingRepo.findExpiredPending(cutoff) DO
        IF booking.paidAmount > 0 THEN
            CONTINUE                              # បង់ខ្លះហើយ — ទុកឲ្យ admin សម្រេច

        SET booking.status       = CANCELLED
        SET booking.cancelReason = "លុបចោលស្វ័យប្រវត្តិ៖ មិនបានទូទាត់ក្នុង ២៤ ម៉ោង"
        SET booking.cancelledAt  = now()
        bookingRepo.save(booking)

        schedule = booking.schedule
        IF schedule.status == FULL THEN
            SET schedule.status = OPEN
            scheduleRepo.save(schedule)

        CALL notificationService.sendBookingAutoCancelled(booking)

END FUNCTION
```

## F7.9 Controller

```
CONTROLLER BookingController  base = "/api/v1/bookings"

    POST   "/"                       201   @Valid CreateBookingRequest, Authentication
           -> RETURN service.bookTour(body, auth.getName())

    GET    "/me"                     200   status(optional), page=0, size=10, Authentication
           -> RETURN service.findMyBookings(auth.getName(), status, page, size)

    GET    "/{code}"                 200   Authentication
           -> RETURN service.findByCode(code, auth.getName(), hasRole("ADMIN"))

    PATCH  "/{code}/cancel"          200   @Valid CancelBookingRequest, Authentication
    PATCH  "/{code}/passengers"      200   @Valid UpdatePassengersRequest, Authentication
    PATCH  "/{code}/confirm"         200                                     [ADMIN]
    GET    "/"                       200   status, fromDate, toDate, page, size  [ADMIN]
```

---

# F8 — Payment (ការទូទាត់) ⭐

**គោលបំណង** : កត់ត្រាការទូទាត់ · ការសងប្រាក់វិញ · ធានាភាពត្រឹមត្រូវនៃទឹកប្រាក់។

## F8.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC8.1 | អតិថិជនបង់ប្រាក់ | `POST /api/v1/bookings/{code}/payments` | CUSTOMER |
| UC8.2 | មើលប្រវត្តិទូទាត់នៃការកក់ | `GET /api/v1/bookings/{code}/payments` | CUSTOMER/ADMIN |
| UC8.3 | Admin បញ្ជាក់ការទូទាត់ដោយដៃ | `PATCH /api/v1/payments/{ref}/verify` | ADMIN |
| UC8.4 | Admin បដិសេធការទូទាត់ | `PATCH /api/v1/payments/{ref}/reject` | ADMIN |
| UC8.5 | Admin បង្កើតការសងប្រាក់វិញ | `POST /api/v1/bookings/{code}/refunds` | ADMIN |
| UC8.6 | Admin មើលរបាយការណ៍ចំណូល | `GET /api/v1/payments/report` | ADMIN |

## F8.2 Entity

```
ENTITY Payment EXTENDS BaseEntity  -> table "payments"
    referenceNo    : String         @Column(unique, nullable=false, length=40)  # PM-20260912-0001
    booking        : Booking        @ManyToOne(LAZY, optional=false)
    type           : PaymentType    @Enumerated(STRING)
    method         : PaymentMethod  @Enumerated(STRING)
    amount         : BigDecimal     @Column(nullable=false, precision=12, scale=2)
    currency       : String         @Column(nullable=false, length=3)  DEFAULT "USD"
    status         : PaymentStatus  @Enumerated(STRING)
    transactionId  : String         @Column(length=120)   # លេខពី payment gateway
    receiptUrl     : String         @Column(length=255)   # រូបភាពវិក្កយបត្រ
    note           : String         @Column(length=500)
    rejectReason   : String         @Column(length=500)
    paidAt         : LocalDateTime
    verifiedAt     : LocalDateTime
    verifiedBy     : String         @Column(length=60)

ENUM PaymentType   : DEPOSIT, FULL_PAYMENT, BALANCE, REFUND
ENUM PaymentMethod : CASH, BANK_TRANSFER, ABA_PAY, WING, CREDIT_CARD, KHQR
ENUM PaymentStatus : PENDING, VERIFIED, REJECTED, REFUNDED
```

## F8.3 វិន័យអាជីវកម្ម

| # | វិន័យ | កំហុស |
|---|---|---|
| BR1 | ទឹកប្រាក់ត្រូវធំជាង 0 | 400 |
| BR2 | បង់មិនបានលើការកក់ `CANCELLED` ឬ `COMPLETED` | 409 |
| BR3 | ទឹកប្រាក់បង់ + បង់រួច មិនត្រូវលើស `totalPrice` | 422 |
| BR4 | `DEPOSIT` ត្រូវ ≥ ៣០% នៃ `totalPrice` | 422 |
| BR5 | `BANK_TRANSFER` ត្រូវមាន `receiptUrl` | 400 |
| BR6 | បញ្ជាក់ (verify) បានតែស្ថានភាព `PENDING` | 409 |
| BR7 | សងវិញ មិនត្រូវលើសទឹកប្រាក់ដែលបញ្ជាក់រួច | 422 |

## F8.4 DTOs

```
DTO CreatePaymentRequest:
    type          : PaymentType    @NotNull
    method        : PaymentMethod  @NotNull
    amount        : BigDecimal     @NotNull @DecimalMin("0.01")
    transactionId : String         @Size(max=120)
    receiptUrl    : String         @Size(max=255)
    note          : String         @Size(max=500)

DTO RejectPaymentRequest:
    reason : String  @NotBlank @Size(max=500)

DTO CreateRefundRequest:
    amount : BigDecimal  @NotNull @DecimalMin("0.01")
    reason : String      @NotBlank @Size(max=500)
    method : PaymentMethod  @NotNull

DTO PaymentResponse:
    referenceNo, bookingCode
    type, method, amount, currency, status
    transactionId, receiptUrl, note, rejectReason
    paidAt, verifiedAt, verifiedBy

DTO PaymentSummaryResponse:
    bookingCode
    totalPrice      : BigDecimal
    verifiedAmount  : BigDecimal
    pendingAmount   : BigDecimal
    refundedAmount  : BigDecimal
    remainingAmount : BigDecimal
    isFullyPaid     : boolean
    payments        : List<PaymentResponse>

DTO RevenueReportResponse:
    fromDate, toDate
    totalRevenue    : BigDecimal
    totalRefunded   : BigDecimal
    netRevenue      : BigDecimal
    paymentCount    : long
    byMethod        : Map<PaymentMethod, BigDecimal>
```

## F8.5 Repository

```
REPOSITORY PaymentRepository EXTENDS JpaRepository<Payment, Long>

    findByReferenceNoAndIsDeletedFalse(ref) -> Optional<Payment>
    existsByReferenceNo(ref)                -> boolean
    findAllByBookingIdOrderByCreatedAtDesc(bookingId) -> List<Payment>
    existsByTransactionId(txId)             -> boolean

    @Query sumVerifiedAmount(bookingId) -> BigDecimal
        JPQL: SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
             WHERE p.booking.id = :bookingId
               AND p.status = 'VERIFIED' AND p.type <> 'REFUND'
               AND p.isDeleted = false

    @Query sumPendingAmount(bookingId) -> BigDecimal
    @Query sumRefundedAmount(bookingId) -> BigDecimal
        JPQL: SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
             WHERE p.booking.id = :bookingId
               AND p.type = 'REFUND' AND p.status IN ('VERIFIED','REFUNDED')

    @Query revenueBetween(from, to) -> BigDecimal
        JPQL: SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
             WHERE p.status = 'VERIFIED' AND p.type <> 'REFUND'
               AND p.isDeleted = false
               AND DATE(p.paidAt) BETWEEN :from AND :to

    @Query refundedBetween(from, to) -> BigDecimal
        JPQL: SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
             WHERE p.type = 'REFUND' AND p.status IN ('VERIFIED','REFUNDED')
               AND p.isDeleted = false
               AND DATE(p.paidAt) BETWEEN :from AND :to

    @Query countBetween(from, to)    -> long
    @Query revenueByMethod(from, to) -> List<Object[]>   # [method, sum]
    @Query countTodayPayments() -> long
```

## F8.6 Service — UC8.1 បង់ប្រាក់

```
FUNCTION pay(bookingCode, req, username) -> PaymentResponse:    # @Transactional

    # ═══ ២. LOAD ═══
    booking = bookingRepo.findByCodeAndIsDeletedFalse(bookingCode)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការកក់លេខ " + bookingCode)

    # ═══ ៣. CHECK RULES ═══
    IF booking.customer.username != username THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិទូទាត់លើការកក់នេះទេ")

    IF booking.status == CANCELLED THEN                                        # BR2
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ការកក់នេះត្រូវបានលុបចោលហើយ")

    IF booking.status == COMPLETED THEN                                        # BR2
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ដំណើរបានបញ្ចប់ហើយ")

    IF req.type == REFUND THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "សូមប្រើ endpoint សងប្រាក់វិញជំនួស")

    IF req.method == BANK_TRANSFER AND req.receiptUrl IS EMPTY THEN            # BR5
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "ការផ្ទេរតាមធនាគារត្រូវភ្ជាប់រូបភាពវិក្កយបត្រ")

    IF req.transactionId IS NOT NULL
       AND paymentRepo.existsByTransactionId(req.transactionId) THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "លេខប្រតិបត្តិការនេះត្រូវបានប្រើរួចហើយ")

    # ═══ ៤. CALCULATE ═══
    verified  = paymentRepo.sumVerifiedAmount(booking.id)
    pending   = paymentRepo.sumPendingAmount(booking.id)
    refunded  = paymentRepo.sumRefundedAmount(booking.id)
    committed = verified + pending - refunded
    remaining = booking.totalPrice - committed

    IF remaining <= 0 THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ការកក់នេះបានទូទាត់គ្រប់ចំនួនរួចហើយ")

    IF req.amount > remaining THEN                                             # BR3
        THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, 
            "ទឹកប្រាក់លើសចំនួនដែលនៅសល់ — នៅសល់ត្រូវបង់ " + remaining)

    IF req.type == DEPOSIT THEN                                                # BR4
        minDeposit = booking.totalPrice * 0.30
        IF req.amount < minDeposit THEN
            THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ប្រាក់កក់ត្រូវយ៉ាងតិច ៣០% (" + minDeposit + ")")

    IF req.type == FULL_PAYMENT AND req.amount != remaining THEN
        THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "FULL_PAYMENT ត្រូវស្មើនឹងចំនួននៅសល់ (" + remaining + ")")

    # ═══ ៥. BUILD ═══
    payment = NEW Payment
    SET payment.uuid          = randomUUID()
    SET payment.referenceNo   = generatePaymentRef()          # PM-20260912-0001
    SET payment.booking       = booking
    SET payment.type          = req.type
    SET payment.method        = req.method
    SET payment.amount        = req.amount
    SET payment.currency      = "USD"
    SET payment.transactionId = req.transactionId
    SET payment.receiptUrl    = req.receiptUrl
    SET payment.note          = req.note
    SET payment.paidAt        = now()
    SET payment.isDeleted     = false

    # ការទូទាត់សាច់ប្រាក់តាមអនឡាញ -> បញ្ជាក់ភ្លាម; ការផ្ទេរ -> រង់ចាំ admin
    IF req.method IN (ABA_PAY, KHQR, CREDIT_CARD, WING) THEN
        SET payment.status     = VERIFIED
        SET payment.verifiedAt = now()
        SET payment.verifiedBy = "SYSTEM"
    ELSE
        SET payment.status     = PENDING

    # ═══ ៦. SAVE ═══
    saved = paymentRepo.save(payment)

    # ═══ ៧. SIDE EFFECTS ═══
    IF saved.status == VERIFIED THEN
        CALL syncBookingPaidAmount(booking)

    CALL notificationService.sendPaymentReceived(saved)

    RETURN mapper.toResponse(saved)

END FUNCTION


FUNCTION syncBookingPaidAmount(booking) -> void:
    verified = paymentRepo.sumVerifiedAmount(booking.id)
    refunded = paymentRepo.sumRefundedAmount(booking.id)
    SET booking.paidAmount = verified - refunded
    bookingRepo.save(booking)

    # បង់គ្រប់ + នៅ PENDING -> បញ្ជាក់ការកក់ដោយស្វ័យប្រវត្តិ
    IF booking.paidAmount >= booking.totalPrice AND booking.status == PENDING THEN
        SET booking.status      = CONFIRMED
        SET booking.confirmedAt = now()
        bookingRepo.save(booking)
        CALL notificationService.sendBookingConfirmed(booking)
END FUNCTION


FUNCTION generatePaymentRef() -> String:
    datePart = format(today(), "yyyyMMdd")
    SET seq  = paymentRepo.countTodayPayments() + 1
    SET ref  = "PM-" + datePart + "-" + padLeft(seq, 4, '0')
    WHILE paymentRepo.existsByReferenceNo(ref) DO
        SET seq = seq + 1
        SET ref = "PM-" + datePart + "-" + padLeft(seq, 4, '0')
    RETURN ref
END FUNCTION
```

## F8.7 Service — មុខងារផ្សេងទៀត

```
FUNCTION verify(referenceNo, adminUsername) -> PaymentResponse:   # @Transactional [ADMIN]

    payment = paymentRepo.findByReferenceNoAndIsDeletedFalse(referenceNo)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការទូទាត់លេខ " + referenceNo)

    IF payment.status != PENDING THEN                                          # BR6
        THROW ResponseStatusException(HttpStatus.CONFLICT, "បញ្ជាក់បានតែការទូទាត់ស្ថានភាព PENDING (បច្ចុប្បន្ន "
                     + payment.status + ")")

    SET payment.status     = VERIFIED
    SET payment.verifiedAt = now()
    SET payment.verifiedBy = adminUsername
    paymentRepo.save(payment)

    CALL syncBookingPaidAmount(payment.booking)
    CALL notificationService.sendPaymentVerified(payment)

    RETURN mapper.toResponse(payment)

END FUNCTION


FUNCTION reject(referenceNo, req, adminUsername) -> PaymentResponse:   # [ADMIN]

    payment = paymentRepo.findByReferenceNoAndIsDeletedFalse(referenceNo)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការទូទាត់លេខ " + referenceNo)

    IF payment.status != PENDING THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "បដិសេធបានតែការទូទាត់ស្ថានភាព PENDING")

    SET payment.status       = REJECTED
    SET payment.rejectReason = req.reason
    SET payment.verifiedAt   = now()
    SET payment.verifiedBy   = adminUsername
    paymentRepo.save(payment)

    CALL notificationService.sendPaymentRejected(payment)

    RETURN mapper.toResponse(payment)

END FUNCTION


FUNCTION createRefund(booking, amount, reason) -> PaymentResponse:   # @Transactional

    verified = paymentRepo.sumVerifiedAmount(booking.id)
    refunded = paymentRepo.sumRefundedAmount(booking.id)
    maxRefundable = verified - refunded

    IF amount > maxRefundable THEN                                             # BR7
        THROW ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "សងវិញបានច្រើនបំផុត " + maxRefundable)

    refund = NEW Payment
    SET refund.uuid        = randomUUID()
    SET refund.referenceNo = generatePaymentRef()
    SET refund.booking     = booking
    SET refund.type        = REFUND
    SET refund.method      = BANK_TRANSFER          # លំនាំដើម
    SET refund.amount      = amount
    SET refund.status      = PENDING                # រង់ចាំហិរញ្ញវត្ថុដំណើរការ
    SET refund.note        = reason
    SET refund.paidAt      = now()
    SET refund.isDeleted   = false

    saved = paymentRepo.save(refund)

    CALL syncBookingPaidAmount(booking)
    CALL notificationService.sendRefundInitiated(saved)

    RETURN mapper.toResponse(saved)

END FUNCTION


FUNCTION refundFull(booking, reason) -> void:
    verified = paymentRepo.sumVerifiedAmount(booking.id)
    refunded = paymentRepo.sumRefundedAmount(booking.id)
    amount   = verified - refunded

    IF amount <= 0 THEN RETURN                  # គ្មានអ្វីត្រូវសង

    CALL createRefund(booking, amount, reason)
END FUNCTION


FUNCTION getSummary(bookingCode, username, isAdmin) -> PaymentSummaryResponse:

    booking = bookingRepo.findByCodeAndIsDeletedFalse(bookingCode)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការកក់លេខ " + bookingCode)

    IF isAdmin == false AND booking.customer.username != username THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិមើលការទូទាត់នេះទេ")

    verified = paymentRepo.sumVerifiedAmount(booking.id)
    pending  = paymentRepo.sumPendingAmount(booking.id)
    refunded = paymentRepo.sumRefundedAmount(booking.id)

    summary = NEW PaymentSummaryResponse
    SET summary.bookingCode     = booking.code
    SET summary.totalPrice      = booking.totalPrice
    SET summary.verifiedAmount  = verified
    SET summary.pendingAmount   = pending
    SET summary.refundedAmount  = refunded
    SET summary.remainingAmount = booking.totalPrice - (verified - refunded)
    SET summary.isFullyPaid     = (summary.remainingAmount <= 0)
    SET summary.payments        = paymentRepo.findAllByBookingIdOrderByCreatedAtDesc(booking.id)
                                  MAP mapper::toResponse
    RETURN summary

END FUNCTION


FUNCTION getRevenueReport(fromDate, toDate) -> RevenueReportResponse:   # [ADMIN]

    IF fromDate IS NULL THEN SET fromDate = today().withDayOfMonth(1)
    IF toDate   IS NULL THEN SET toDate   = today()

    IF toDate IS BEFORE fromDate THEN
        THROW ResponseStatusException(HttpStatus.BAD_REQUEST, "toDate ត្រូវក្រោយ fromDate")

    report = NEW RevenueReportResponse
    SET report.fromDate      = fromDate
    SET report.toDate        = toDate
    SET report.totalRevenue  = paymentRepo.revenueBetween(fromDate, toDate)
    SET report.totalRefunded = paymentRepo.refundedBetween(fromDate, toDate)
    SET report.netRevenue    = report.totalRevenue - report.totalRefunded
    SET report.paymentCount  = paymentRepo.countBetween(fromDate, toDate)
    SET report.byMethod      = toMap(paymentRepo.revenueByMethod(fromDate, toDate))

    RETURN report

END FUNCTION
```

## F8.8 Controller

```
CONTROLLER BookingPaymentController  base = "/api/v1/bookings/{code}"

    POST   "/payments"      201   @Valid CreatePaymentRequest, Authentication
    GET    "/payments"      200   Authentication
           -> RETURN service.getSummary(code, auth.getName(), hasRole("ADMIN"))
    POST   "/refunds"       201   @Valid CreateRefundRequest              [ADMIN]

CONTROLLER PaymentController  base = "/api/v1/payments"

    PATCH  "/{ref}/verify"  200   Authentication                          [ADMIN]
    PATCH  "/{ref}/reject"  200   @Valid RejectPaymentRequest             [ADMIN]
    GET    "/report"        200   fromDate, toDate                        [ADMIN]
```

---

# F9 — Review (ការវាយតម្លៃ)

**គោលបំណង** : អតិថិជនវាយតម្លៃ Tour បន្ទាប់ពីដំណើរបញ្ចប់ · គណនាពិន្ទុមធ្យម។

## F9.1 Use Cases

| Code | Use Case | Endpoint | សិទ្ធិ |
|---|---|---|---|
| UC9.1 | អតិថិជនផ្តល់ការវាយតម្លៃ | `POST /api/v1/bookings/{code}/review` | CUSTOMER |
| UC9.2 | មើលការវាយតម្លៃរបស់ Tour | `GET /api/v1/tours/{uuid}/reviews` | សាធារណៈ |
| UC9.3 | អតិថិជនកែការវាយតម្លៃ | `PATCH /api/v1/reviews/{uuid}` | CUSTOMER |
| UC9.4 | អតិថិជនលុបការវាយតម្លៃ | `DELETE /api/v1/reviews/{uuid}` | CUSTOMER |
| UC9.5 | Admin ឆ្លើយតបការវាយតម្លៃ | `POST /api/v1/reviews/{uuid}/reply` | ADMIN |
| UC9.6 | Admin លាក់ការវាយតម្លៃមិនសមរម្យ | `PATCH /api/v1/reviews/{uuid}/hide` | ADMIN |
| UC9.7 | មើលសង្ខេបពិន្ទុ (រាប់តាមផ្កាយ) | `GET /api/v1/tours/{uuid}/reviews/summary` | សាធារណៈ |

## F9.2 Entity

```
ENTITY Review EXTENDS BaseEntity  -> table "reviews"
    booking       : Booking        @OneToOne(LAZY, optional=false) @JoinColumn(unique=true)
    tour          : Tour           @ManyToOne(LAZY, optional=false)
    customer      : Customer       @ManyToOne(LAZY, optional=false)
    rating        : Integer        @Column(nullable=false)     # 1..5
    guideRating   : Integer                                     # 1..5, optional
    valueRating   : Integer                                     # 1..5, optional
    title         : String         @Column(length=180)
    comment       : String         @Column(columnDefinition = "TEXT")
    imageUrls     : List<String>   @ElementCollection
    isVisible     : Boolean        @Column(nullable=false)  DEFAULT true
    hiddenReason  : String         @Column(length=255)
    adminReply    : String         @Column(columnDefinition = "TEXT")
    repliedAt     : LocalDateTime
    repliedBy     : String         @Column(length=60)
```

## F9.3 វិន័យអាជីវកម្ម

| # | វិន័យ | កំហុស |
|---|---|---|
| BR1 | ការកក់ត្រូវស្ថិតក្នុងស្ថានភាព `COMPLETED` | 409 |
| BR2 | អ្នកវាយតម្លៃត្រូវជាម្ចាស់ការកក់ | 403 |
| BR3 | ការកក់មួយវាយតម្លៃបានតែម្តង | 409 |
| BR4 | `rating` ត្រូវនៅចន្លោះ ១–៥ | 400 |
| BR5 | វាយតម្លៃបានក្នុងរយៈពេល ៦០ ថ្ងៃបន្ទាប់ពីត្រឡប់ | 409 |
| BR6 | កែបានក្នុងរយៈពេល ៧ ថ្ងៃបន្ទាប់ពីបង្កើត | 409 |

## F9.4 DTOs

```
DTO CreateReviewRequest:
    rating       : Integer       @NotNull @Min(1) @Max(5)
    guideRating  : Integer       @Min(1) @Max(5)
    valueRating  : Integer       @Min(1) @Max(5)
    title        : String        @Size(max=180)
    comment      : String        @NotBlank @Size(min=10, max=3000)
    imageUrls    : List<String>  @Size(max=5)

DTO UpdateReviewRequest:        # គ្រប់ field nullable

DTO ReplyReviewRequest:
    reply : String  @NotBlank @Size(max=2000)

DTO HideReviewRequest:
    reason : String  @NotBlank @Size(max=255)

DTO ReviewResponse:
    uuid, bookingCode
    tourUuid, tourTitle
    customerName        # បង្ហាញឈ្មោះខ្លី ឧ. "សុខ ដ***"
    customerAvatarUrl
    rating, guideRating, valueRating
    title, comment, imageUrls
    adminReply, repliedAt
    createdAt

DTO ReviewSummaryResponse:
    tourUuid
    averageRating       : Double
    totalReviews        : long
    averageGuideRating  : Double
    averageValueRating  : Double
    starCounts          : Map<Integer, Long>    # {5:120, 4:45, 3:10, 2:2, 1:1}
    starPercentages     : Map<Integer, Double>
```

## F9.5 Repository

```
REPOSITORY ReviewRepository EXTENDS JpaRepository<Review, Long>

    findByUuidAndIsDeletedFalse(uuid)  -> Optional<Review>
    existsByBookingId(bookingId)       -> boolean
    findByBookingId(bookingId)         -> Optional<Review>

    @Query findVisibleByTour(tourId, pageable) -> Page<Review>
        JPQL: SELECT r FROM Review r
             WHERE r.tour.id = :tourId AND r.isVisible = true AND r.isDeleted = false
             ORDER BY r.createdAt DESC

    @Query findTop5ByTour(tourId) -> List<Review>

    @Query averageRating(tourId) -> Double
        JPQL: SELECT COALESCE(AVG(r.rating), 0) FROM Review r
             WHERE r.tour.id = :tourId AND r.isVisible = true AND r.isDeleted = false

    @Query averageGuideRating(tourId) -> Double
        JPQL: SELECT COALESCE(AVG(r.guideRating), 0) FROM Review r
             WHERE r.tour.id = :tourId AND r.guideRating IS NOT NULL
               AND r.isVisible = true AND r.isDeleted = false

    @Query averageValueRating(tourId) -> Double
        JPQL: SELECT COALESCE(AVG(r.valueRating), 0) FROM Review r
             WHERE r.tour.id = :tourId AND r.valueRating IS NOT NULL
               AND r.isVisible = true AND r.isDeleted = false

    @Query countVisibleByTour(tourId) -> long

    @Query countByStars(tourId) -> List<Object[]>          # [rating, count]
        JPQL: SELECT r.rating, COUNT(r) FROM Review r
             WHERE r.tour.id = :tourId AND r.isVisible = true AND r.isDeleted = false
             GROUP BY r.rating
```

## F9.6 Service

```
FUNCTION createNew(bookingCode, req, username) -> ReviewResponse:      # @Transactional

    # ═══ ២. LOAD ═══
    booking = bookingRepo.findByCodeAndIsDeletedFalse(bookingCode)
              ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការកក់លេខ " + bookingCode)

    # ═══ ៣. CHECK RULES ═══
    IF booking.customer.username != username THEN                              # BR2
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមែនជាម្ចាស់ការកក់នេះទេ")

    IF booking.status != COMPLETED THEN                                        # BR1
        THROW ResponseStatusException(HttpStatus.CONFLICT, "វាយតម្លៃបានតែក្រោយពេលដំណើរបញ្ចប់ (បច្ចុប្បន្ន "
                     + booking.status + ")")

    IF reviewRepo.existsByBookingId(booking.id) THEN                           # BR3
        THROW ResponseStatusException(HttpStatus.CONFLICT, "អ្នកបានវាយតម្លៃការកក់នេះរួចហើយ")

    daysSinceReturn = daysBetween(booking.schedule.returnDate, today())
    IF daysSinceReturn > 60 THEN                                               # BR5
        THROW ResponseStatusException(HttpStatus.CONFLICT, "រយៈពេលវាយតម្លៃផុតកំណត់ហើយ (អនុញ្ញាតក្នុង ៦០ ថ្ងៃ)")

    # ═══ ៥. BUILD ═══
    review = mapper.toEntity(req)
    SET review.uuid      = randomUUID()
    SET review.booking   = booking
    SET review.tour      = booking.schedule.tour
    SET review.customer  = booking.customer
    SET review.isVisible = true
    SET review.isDeleted = false

    # ═══ ៦. SAVE ═══
    saved = reviewRepo.save(review)

    # ═══ ៧. SIDE EFFECTS — គណនាពិន្ទុមធ្យមឡើងវិញ ═══
    CALL recalculateTourRating(review.tour.id)
    CALL notificationService.notifyNewReview(saved)

    RETURN mapper.toResponse(saved)

END FUNCTION


FUNCTION recalculateTourRating(tourId) -> void:
    tour = tourRepo.findById(tourId) ORELSE RETURN
    SET tour.averageRating = round(reviewRepo.averageRating(tourId), 1)
    SET tour.reviewCount   = reviewRepo.countVisibleByTour(tourId)
    tourRepo.save(tour)
END FUNCTION


FUNCTION updateByUuid(uuid, req, username) -> ReviewResponse:             # @Transactional

    review = reviewRepo.findByUuidAndIsDeletedFalse(uuid)
             ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការវាយតម្លៃ uuid = " + uuid)

    IF review.customer.username != username THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិកែការវាយតម្លៃនេះទេ")

    daysSinceCreated = daysBetween(review.createdAt.toLocalDate(), today())
    IF daysSinceCreated > 7 THEN                                               # BR6
        THROW ResponseStatusException(HttpStatus.CONFLICT, "កែបានក្នុងរយៈពេល ៧ ថ្ងៃបន្ទាប់ពីបង្កើតតែប៉ុណ្ណោះ")

    IF review.isVisible == false THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ការវាយតម្លៃនេះត្រូវបានលាក់ដោយអ្នកគ្រប់គ្រង")

    ratingChanged = (req.rating IS NOT NULL AND req.rating != review.rating)

    mapper.toEntity(req, review)
    saved = reviewRepo.save(review)

    IF ratingChanged THEN
        CALL recalculateTourRating(review.tour.id)

    RETURN mapper.toResponse(saved)

END FUNCTION


FUNCTION deleteByUuid(uuid, username) -> void:                            # @Transactional

    review = reviewRepo.findByUuidAndIsDeletedFalse(uuid)
             ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការវាយតម្លៃ uuid = " + uuid)

    IF review.customer.username != username THEN
        THROW ResponseStatusException(HttpStatus.FORBIDDEN, "អ្នកមិនមានសិទ្ធិលុបការវាយតម្លៃនេះទេ")

    SET review.isDeleted = true
    reviewRepo.save(review)

    CALL recalculateTourRating(review.tour.id)

END FUNCTION


FUNCTION hide(uuid, req, adminUsername) -> ReviewResponse:          # [ADMIN]

    review = reviewRepo.findByUuidAndIsDeletedFalse(uuid)
             ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការវាយតម្លៃ uuid = " + uuid)

    IF review.isVisible == false THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ការវាយតម្លៃនេះត្រូវបានលាក់រួចហើយ")

    SET review.isVisible    = false
    SET review.hiddenReason = req.reason
    reviewRepo.save(review)

    CALL recalculateTourRating(review.tour.id)     # ត្រូវដកចេញពីពិន្ទុមធ្យម

    RETURN mapper.toResponse(review)

END FUNCTION


FUNCTION reply(uuid, req, adminUsername) -> ReviewResponse:         # [ADMIN]

    review = reviewRepo.findByUuidAndIsDeletedFalse(uuid)
             ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញការវាយតម្លៃ uuid = " + uuid)

    IF review.adminReply IS NOT EMPTY THEN
        THROW ResponseStatusException(HttpStatus.CONFLICT, "ការវាយតម្លៃនេះមានការឆ្លើយតបរួចហើយ — សូមប្រើ PATCH ដើម្បីកែ")

    SET review.adminReply = req.reply
    SET review.repliedAt  = now()
    SET review.repliedBy  = adminUsername
    reviewRepo.save(review)

    CALL notificationService.notifyReviewReplied(review)

    RETURN mapper.toResponse(review)

END FUNCTION


FUNCTION findByTour(tourUuid, page, size) -> PageResponse<ReviewResponse>:

    tour = tourRepo.findByUuidAndIsDeletedFalse(tourUuid)
           ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Tour uuid = " + tourUuid)

    pageable = buildPageable(page, size, "createdAt", DESC)
    result   = reviewRepo.findVisibleByTour(tour.id, pageable)

    RETURN PageResponse.from(result MAP mapper::toResponse)

END FUNCTION


FUNCTION getSummary(tourUuid) -> ReviewSummaryResponse:

    tour = tourRepo.findByUuidAndIsDeletedFalse(tourUuid)
           ORELSE THROW ResponseStatusException(HttpStatus.NOT_FOUND, "រកមិនឃើញ Tour uuid = " + tourUuid)

    rows  = reviewRepo.countByStars(tour.id)      # [[5,120],[4,45],...]
    total = SUM of counts IN rows

    starCounts      = NEW Map
    starPercentages = NEW Map
    FOR star FROM 1 TO 5 DO
        count = rows FIND star ORELSE 0
        SET starCounts[star]      = count
        SET starPercentages[star] = IF total > 0 THEN round(count * 100.0 / total, 1) ELSE 0

    summary = NEW ReviewSummaryResponse
    SET summary.tourUuid           = tourUuid
    SET summary.averageRating      = round(reviewRepo.averageRating(tour.id), 1)
    SET summary.totalReviews       = total
    SET summary.averageGuideRating = round(reviewRepo.averageGuideRating(tour.id), 1)
    SET summary.averageValueRating = round(reviewRepo.averageValueRating(tour.id), 1)
    SET summary.starCounts         = starCounts
    SET summary.starPercentages    = starPercentages

    RETURN summary

END FUNCTION
```

## F9.7 Controller

```
CONTROLLER BookingReviewController  base = "/api/v1/bookings/{code}"

    POST   "/review"           201   @Valid CreateReviewRequest, Authentication

CONTROLLER TourReviewController  base = "/api/v1/tours/{tourUuid}/reviews"

    GET    "/"                 200   page=0, size=10
    GET    "/summary"          200

CONTROLLER ReviewController  base = "/api/v1/reviews"

    PATCH  "/{uuid}"           200   @Valid UpdateReviewRequest, Authentication
    DELETE "/{uuid}"           204   Authentication
    POST   "/{uuid}/reply"     201   @Valid ReplyReviewRequest        [ADMIN]
    PATCH  "/{uuid}/hide"      200   @Valid HideReviewRequest         [ADMIN]
```

---

# X — Cross-cutting (ផ្នែកឆ្លងកាត់)

## X.1 Global Exception Handler

```
DTO ApiErrorResponse:
    timestamp  : LocalDateTime
    status     : int
    error      : String
    message    : String
    path       : String
    fieldErrors: Map<String, String>       # សម្រាប់ validation

HANDLER GlobalAppException:            # @RestControllerAdvice

    ON MethodArgumentNotValidException -> 400
        fieldErrors = COLLECT (field -> message) FROM ex.bindingResult
        RETURN ApiErrorResponse(400, "Validation Failed",
                                "ទិន្នន័យបញ្ចូលមិនត្រឹមត្រូវ", fieldErrors)

    ON ResponseStatusException -> ex.statusCode
        RETURN ApiErrorResponse(ex.statusCode, ex.reason, ...)

    ON DataIntegrityViolationException -> 409
        RETURN ApiErrorResponse(409, "Conflict", "ទិន្នន័យប៉ះទង្គិចនឹងទិន្នន័យដែលមានស្រាប់")

    ON OptimisticLockingFailureException -> 409
        RETURN ApiErrorResponse(409, "Conflict",
                                "ទិន្នន័យត្រូវបានកែដោយអ្នកផ្សេង សូមព្យាយាមម្តងទៀត")

    ON AccessDeniedException -> 403
        RETURN ApiErrorResponse(403, "Forbidden", "អ្នកគ្មានសិទ្ធិលើប្រតិបត្តិការនេះ")

    ON Exception -> 500
        LOG ex WITH stack trace
        RETURN ApiErrorResponse(500, "Internal Server Error",
                                "មានបញ្ហាបច្ចេកទេស សូមទាក់ទងអ្នកគ្រប់គ្រង")
```

## X.2 Utils

```
UTIL GenerateUtils:

    FUNCTION randomUUID() -> String
        RETURN UUID.randomUUID().toString()

    FUNCTION generateSequentialCode(prefix, currentCount) -> String
        RETURN prefix + "-" + padLeft(currentCount + 1, 4, '0')     # GD-0001

    FUNCTION generateDateCode(prefix, date) -> String
        RETURN prefix + "-" + format(date, "yyyyMMdd") + "-" + randomDigits(2)

    FUNCTION generateUniqueSlug(text, existsChecker) -> String
        base = toLowerCase(text) REPLACE non-alphanumeric WITH "-"
        SET slug = base; SET n = 1
        WHILE existsChecker(slug) DO
            SET slug = base + "-" + n; SET n = n + 1
        RETURN slug


UTIL DateUtils:

    FUNCTION daysBetween(from, to) -> long
    FUNCTION yearsBetween(from, to) -> int
    FUNCTION isPast(date) -> boolean
    FUNCTION overlaps(startA, endA, startB, endB) -> boolean
        RETURN startA <= endB AND endA >= startB
```

## X.3 Security (សង្ខេប)

```
CONFIG SecurityConfig:

    PUBLIC (permitAll):
        GET  /api/v1/tours/**
        GET  /api/v1/categories/**
        GET  /api/v1/destinations/**
        GET  /api/v1/guides/{uuid}
        POST /api/v1/customers/register
        /swagger-ui/**, /v3/api-docs/**

    ROLE_CUSTOMER:
        /api/v1/customers/me, /api/v1/bookings/**, /api/v1/reviews/**

    ROLE_ADMIN:
        គ្រប់ endpoint ដែលនៅសល់

UTIL AuthUtils:
    FUNCTION currentUsername(auth) -> String
        IF auth IS NULL THEN THROW ResponseStatusException(HttpStatus.UNAUTHORIZED, "សូម login សិន")
        RETURN auth.getName()

    FUNCTION isAdmin(auth) -> boolean
        RETURN auth.authorities CONTAINS "ROLE_ADMIN"
```

## X.4 លំដាប់អនុវត្ត (Implementation Order)

សរសេរកូដតាមលំដាប់នេះ — មួយៗត្រូវពឹងលើមួយមុន៖

```
ដំណាក់កាល ១ — គ្រឹះ
  [1] base/BaseEntity, PageResponse, PageMapper
  [2] config/JpaAuditingConfig
  [3] exception/ApiErrorResponse, GlobalAppException
  [4] utils/GenerateUtils, DateUtils

ដំណាក់កាល ២ — Master Data (គ្មានការពឹងផ្អែក)
  [5] F1 Category
  [6] F2 Destination
  [7] F3 Guide

ដំណាក់កាល ៣ — ខ្លឹមសារ
  [8] F4 Tour            (ពឹង Category + Destination)
  [9] F5 Schedule        (ពឹង Tour + Guide)

ដំណាក់កាល ៤ — អ្នកប្រើ និងប្រតិបត្តិការ
  [10] F6 Customer
  [11] F7 Booking        (ពឹង Customer + Schedule)
  [12] F8 Payment        (ពឹង Booking)
  [13] F9 Review         (ពឹង Booking + Tour)

ដំណាក់កាល ៥ — បន្ថែម
  [14] security/SecurityConfig
  [15] Scheduled jobs (refreshScheduleStatuses, autoCancelExpiredPending)
  [16] notification service
  [17] Swagger / OpenAPI
```

## X.5 បញ្ជីត្រួតពិនិត្យមុនចាប់ផ្តើមកូដ Feature នីមួយៗ

- [ ] រាយ Use Case គ្រប់គ្រាន់ហើយឬនៅ?
- [ ] កំណត់ Entity + field + relationship + constraint រួចហើយឬនៅ?
- [ ] រៀបចំ DTO ដាច់ពី Entity (Request / Response) ហើយឬនៅ?
- [ ] រាល់ `find...` មាន `ORELSE THROW` ហើយឬនៅ?
- [ ] រាល់ការបង្កើត មានពិនិត្យស្ទួន (unique) ហើយឬនៅ?
- [ ] រាល់ការប្តូរស្ថានភាព មានពិនិត្យស្ថានភាពមុនហើយឬនៅ?
- [ ] Function ដែលកែច្រើនតារាង បានដាក់ `@Transactional` ហើយឬនៅ?
- [ ] Business logic ស្ថិតក្នុង Service (មិនមែន Controller) ហើយឬនៅ?
- [ ] សិទ្ធិ (ម្ចាស់ធនធាន / ADMIN) បានពិនិត្យហើយឬនៅ?

---

**បន្ទាប់** : ចាប់ផ្តើមកូដពី **ដំណាក់កាល ១** (base + exception + utils) មុនគេ ព្រោះគ្រប់ feature ពឹងលើវា។
