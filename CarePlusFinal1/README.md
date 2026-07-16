# CarePlus Hospital Patient Management System

CarePlus is a Java socket-based client/server system for CIT3009 Advanced Programming.

## Included Features

- TCP/IP socket client/server architecture
- Object serialization with `CarePlusRequest` and `CarePlusResponse`
- Multithreaded server using `ExecutorService`
- Patient client and employee client launch modes
- MDI Swing GUI with `JDesktopPane` and internal frames
- Patient authentication and employee authentication
- Patient complaint submission
- Appointment inquiry
- Previous complaints and responses
- Payment history
- Receptionist dashboard grouped by complaint category
- Complaint response and staff assignment flow
- Doctor/nurse medical record handling
- Live chat request flow with operating-hour validation
- Log4j2 console and rolling file logging
- Spring XML inversion of control
- MySQL schema with normalized tables and sample data
- JDBC repository implementation
- Hibernate repository/mapping starter implementation

## Sample Logins

Patient:

```text
ID: P1001
Password: pass123
```

Employee:

```text
Doctor: D2001 / staff123
Nurse: N3001 / staff123
Receptionist: R4001 / staff123
```

## Running In VS Code

Open this folder in VS Code:

```text
CarePlusFinal
```

Use **Run and Debug**:

1. Start `CarePlus Server`.
2. Start `CarePlus Patient Client`.
3. Start `CarePlus Employee Client`.

The server must be running before a client can log in.

## Manual Run Commands

From the project folder:

```powershell
javac --release 8 -proc:none -cp "lib/*" -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
Copy-Item src\applicationContext.xml,src\log4j2.xml,src\hibernate.cfg.xml,src\schema.sql -Destination bin -Force
New-Item -ItemType Directory -Force -Path bin\careplus\common\model | Out-Null
Copy-Item src\careplus\common\model\*.hbm.xml -Destination bin\careplus\common\model -Force
```

Server:

```powershell
java -cp "bin;lib/*" App server
```

Patient client:

```powershell
java -cp "bin;lib/*" App patient
```

Employee client:

```powershell
java -cp "bin;lib/*" App employee
```

## Logging

Logs are written to:

```text
logs/careplus-app.log
```

## Database

Run `src/schema.sql` in MySQL 8 to create the database and sample data.

The default Spring bean uses `InMemoryHospitalRepository` so the application can run immediately for demonstration.

To switch to JDBC, edit `src/applicationContext.xml` and replace:

```xml
<bean id="hospitalRepository" class="careplus.server.repository.InMemoryHospitalRepository" />
```

with:

```xml
<bean id="hospitalRepository" class="careplus.server.repository.JdbcHospitalRepository">
    <constructor-arg value="localhost" />
    <constructor-arg value="careplus_hospital" />
    <constructor-arg value="root" />
    <constructor-arg value="" />
</bean>
```

To switch to Hibernate, use:

```xml
<bean id="hospitalRepository" class="careplus.server.repository.HibernateHospitalRepository" />
```
