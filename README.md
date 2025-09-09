# 📦 Adreskobox

[🇸🇰 Slovensky](#-slovensky) | [🇬🇧 English](#-english)

---

## 🇸🇰 Slovensky

Adreskobox je JavaFX aplikácia určená na **generovanie PDF etikiet a podacích lístkov** zo súborov **CSV** alebo **XLSX**.  
Projekt je vo fáze vývoja a beží pomocou **Maven JavaFX pluginu** alebo priamo z **IntelliJ IDEA**.

---

### ✨ Funkcionalita podľa kariet aplikácie

#### 1️⃣ Import údajov
- Možnosť importovať **CSV** alebo **XLSX** súbor
- Automatická detekcia:
    - typu súboru
    - prítomnosti hlavičky
    - oddeľovača v CSV (`,` `;` tabulátor a pod.)
- Výber formátu štítkov alebo definovanie **vlastných rozmerov štítkov**

#### 2️⃣ Výber rodičov
- Výber rodičov, ktorých mená a adresy sa majú vytlačiť
- Možnosť **Vybrať všetkých** alebo **Zrušiť výber**

#### 3️⃣ Kontrola adries
- Kontrola, či sa adresa zmestí na štítok
- Dlhé adresy sú **označené červenou farbou**
- Možnosť pridať **automatické skratky** (napr. `nám.` namiesto `námestie`)
- Úprava celej adresy alebo len doplnenie skratky

#### 4️⃣ Odosielateľ a podací hárok
- Vyplnenie údajov odosielateľa (automaticky sa uložia pre ďalšie použitie)
- Výber prednastaveného **podacieho hárku** s možnosťou úpravy

#### 5️⃣ Export a protokol
- Nastavenie výstupného priečinka
- Súhrn počtu vygenerovaných súborov
- **Protokol generovania** – zobrazuje priebeh, chyby a správy systému
- Možnosť **otvoriť výstupný adresár** po generovaní

---

### 🛠 Použité technológie
- **Java 21**
- **JavaFX 21**
- **Maven**
- ControlsFX, ValidatorFX, Ikonli
- OpenCSV, Apache POI
- iTextPDF
- SQLite JDBC
- Log4j2
- JUnit 5

---

### 🚀 Spustenie projektu

#### 1. Spustenie cez Maven
Uisti sa, že máš nainštalovaný JDK **21+** a Maven. Potom stačí spustiť:
```bash
mvn clean javafx:run
```

## 🇬🇧 English

Adreskobox is a JavaFX application designed for **generating PDF labels and mailing forms** from **CSV** or **XLSX** files.  
The project is currently in development and runs via the **Maven JavaFX plugin** or directly from **IntelliJ IDEA**.

---

### ✨ Features by Tabs

#### 1️⃣ Data Import
- Import **CSV** or **XLSX** files  
- Automatic detection of:
  - file type  
  - header presence  
  - CSV delimiter (`,` `;` tab, etc.)  
- Choose predefined label formats or define **custom label sizes**  

#### 2️⃣ Parent Selection
- Select parents whose names and addresses should be printed  
- Options: **Select All** or **Deselect All**  

#### 3️⃣ Address Validation
- Check if the address fits on a label  
- Long addresses are **highlighted in red**  
- Option to add **automatic abbreviations** (e.g., `nám.` instead of `námestie`)  
- Edit the entire address or only add abbreviations  

#### 4️⃣ Sender and Mailing Form
- Enter sender details (automatically saved for future use)  
- Choose a predefined **mailing form** with an option to modify it  

#### 5️⃣ Export and Log
- Define output directory  
- Summary of the number of generated files  
- **Generation log** – displays process, errors, and system messages  
- Option to **open output directory** after generation  

---

### 🛠 Technologies Used
- **Java 21**
- **JavaFX 21**
- **Maven**
- ControlsFX, ValidatorFX, Ikonli
- OpenCSV, Apache POI
- iTextPDF
- SQLite JDBC
- Log4j2
- JUnit 5

---

### 🚀 Running the Project

#### 1. Run with Maven
Make sure you have JDK **21+** and Maven installed. Then simply run:

```bash
mvn clean javafx:run
```
