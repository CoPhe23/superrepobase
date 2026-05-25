@echo off
title Vizsga Setup - PNPM + ES Module Backend
color 0A

echo ============================================
echo      	  VIZSGA SETUP
echo ============================================
echo.

cd /d "%~dp0"

echo [1/8] ZIP kicsomagolasa...
if exist "forras.zip" (
    powershell -Command "Expand-Archive -Force 'forras.zip' '.'"
)

echo.
echo [2/8] Frontend pnpm install...
if exist "frontend" (
    cd frontend
    call pnpm install
    cd ..
) else (
    echo Nincs frontend mappa.
)

echo.
echo [3/8] Backend mappa ellenorzese...
if not exist "backend" mkdir backend
cd backend

echo.
echo [4/8] Backend package.json letrehozasa...
if not exist package.json (
(
echo {
echo   "name": "backend",
echo   "version": "1.0.0",
echo   "type": "module",
echo   "scripts": {
echo     "dev": "nodemon server.js",
echo     "start": "node server.js"
echo   },
echo   "dependencies": {},
echo   "devDependencies": {}
echo }
) > package.json
)

echo.
echo [5/8] Backend csomagok telepitese pnpm-mel...
call pnpm add express mysql cors dotenv
call pnpm add -D nodemon

echo.
echo [6/8] configDB.js letrehozasa...
if not exist configDB.js (
(
echo export const configDB = {
echo     host: "localhost",
echo     user: "root",
echo     password: "",
echo     database: "restaurant"
echo }
) > configDB.js
)

echo.
echo [7/8] server.js vizsgasablon letrehozasa...
if not exist server.js (
(
echo import express from "express";
echo import mysql from "mysql";
echo import cors from "cors";
echo import { configDB } from "./configDB.js";
echo.
echo const db = mysql.createConnection^(configDB^);
echo const app = express^(^);
echo.
echo app.use^(cors^(^)^);
echo app.use^(express.json^(^)^);
echo.
echo const PORT = 8000;
echo.
echo app.get^("/", ^(req, res^) =^> {
echo     res.json^({ msg: "Backend mukodik!" }^);
echo }^);
echo.
echo app.get^("/api/categories", ^(req, res^) =^> {
echo     const sql = "SELECT * FROM categories ORDER BY name ASC";
echo.
echo     db.query^(sql, ^(error, result^) =^> {
echo         if ^(error^) {
echo             console.log^(error^);
echo             return res.status^(500^).json^({ error: "Adatbazis hiba!" }^);
echo         }
echo.
echo         res.status^(200^).json^(result^);
echo     }^);
echo }^);
echo.
echo app.get^("/api/foodsbycateg/:categId", ^(req, res^) =^> {
echo     const { categId } = req.params;
echo.
echo     const sql = `
echo         SELECT foods.*, categories.name
echo         FROM foods
echo         INNER JOIN categories ON foods.categId = categories.id
echo         WHERE foods.categId = ?
echo     `;
echo.
echo     db.query^(sql, [categId], ^(error, result^) =^> {
echo         if ^(error^) {
echo             console.log^(error^);
echo             return res.status^(500^).json^({ error: "Adatbazis hiba!" }^);
echo         }
echo.
echo         res.status^(200^).json^(result^);
echo     }^);
echo }^);
echo.
echo app.get^("/api/foodsbysearch/:searchedWord", ^(req, res^) =^> {
echo     const { searchedWord } = req.params;
echo.
echo     const sql = `
echo         SELECT foods.*, categories.name
echo         FROM foods
echo         INNER JOIN categories ON foods.categId = categories.id
echo         WHERE foods.title LIKE ?
echo     `;
echo.
echo     db.query^(sql, [`%%${searchedWord}%%`], ^(error, result^) =^> {
echo         if ^(error^) {
echo             console.log^(error^);
echo             return res.status^(500^).json^({ error: "Adatbazis hiba!" }^);
echo         }
echo.
echo         if ^(result.length === 0^) {
echo             return res.status^(404^).json^({ msg: "The search returned no results" }^);
echo         }
echo.
echo         res.status^(200^).json^(result^);
echo     }^);
echo }^);
echo.
echo app.put^("/api/food/:id", ^(req, res^) =^> {
echo     const { id } = req.params;
echo     const { price } = req.body;
echo.
echo     if ^(!price^) {
echo         return res.status^(400^).json^({ error: "Minden mezo kitoltese kotelezo." }^);
echo     }
echo.
echo     const sql = "UPDATE foods SET price = ? WHERE id = ?";
echo.
echo     db.query^(sql, [price, id], ^(error, result^) =^> {
echo         if ^(error^) {
echo             console.log^(error^);
echo             return res.status^(500^).json^({ error: "Adatbazis hiba!" }^);
echo         }
echo.
echo         if ^(result.affectedRows === 0^) {
echo             return res.status^(404^).json^({ error: "A megadott etel nem letezik!" }^);
echo         }
echo.
echo         res.status^(200^).json^({ msg: "Successfully updated!" }^);
echo     }^);
echo }^);
echo.
echo app.post^("/api/categories", ^(req, res^) =^> {
echo     const { name, photo, descr } = req.body;
echo.
echo     if ^(!name ^|^| !photo ^|^| !descr^) {
echo         return res.status^(400^).json^({ error: "Minden mezo kitoltese kotelezo." }^);
echo     }
echo.
echo     const sql = "INSERT INTO categories (name, photo, descr) VALUES (?, ?, ?)";
echo.
echo     db.query^(sql, [name, photo, descr], ^(error, result^) =^> {
echo         if ^(error^) {
echo             console.log^(error^);
echo             return res.status^(500^).json^({ error: "Adatbazis hiba!" }^);
echo         }
echo.
echo         res.status^(201^).json^({ msg: "Sikeres - Item successfully added!" }^);
echo     }^);
echo }^);
echo.
echo app.listen^(PORT, ^(^) =^> console.log^(`server listening on port: ${PORT}`^)^);
) > server.js
)

echo.
echo [8/8] VS Code inditasa...
cd ..
code .

echo.
echo ============================================
echo              KESZ
echo ============================================
echo.
echo Frontend:
echo     cd frontend
echo     pnpm dev
echo.
echo Backend:
echo     cd backend
echo     pnpm dev
echo.
pause
