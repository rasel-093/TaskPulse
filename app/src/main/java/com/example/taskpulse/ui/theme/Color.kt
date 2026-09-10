package com.example.taskpulse.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Backgrounds & Structure
// ==========================================
val Fog = Color(0xFFE9E7E1)         // Base background — soft grey-stone, not warm cream, not cold blue
val Paper = Color(0xFFF6F4EF)       // Cards — slightly lighter than the base so they lift without a shadow
val PaperShade = Color(0xFFDEDBD2)  // Nested surfaces, pressed states
val Rule = Color(0xFFC8C4B8)        // Borders, dividers — faint pencil rule on paper

// ==========================================
// Text
// ==========================================
val Ink = Color(0xFF2B2924)         // High-emphasis text — soft near-black, warmer and less harsh than pure black
val Graphite = Color(0xFF6E6A5F)    // Secondary text
val GraphiteFaint = Color(0xFFA6A196) // Placeholder, disabled, done-state strikethrough

// ==========================================
// Accents (Confident primary, two quiet functional colors)
// ==========================================
val Indigo = Color(0xFF3C4A6B)      // Primary action / active pulse — deep muted navy-indigo
val IndigoDim = Color(0x263C4A6B)
val Ochre = Color(0xFFA87B2E)       // Recurring reminders — dry, earthy yellow-brown, like an old ledger stamp
val OchreDim = Color(0x26A87B2E)
val Moss = Color(0xFF5B7355)        // Done / complete — dulled green, plant-adjacent
val MossDim = Color(0x265B7355)

// ==========================================
// Semantic Aliases & Compatibility Tokens
// ==========================================
val TextHi = Ink
val TextLo = Graphite
val TextFaint = GraphiteFaint

val Pulse = Indigo
val PulseDim = IndigoDim
val Amber = Ochre
val AmberDim = OchreDim
val Teal = Moss
val TealDim = MossDim

val Panel = Paper
val PanelRaised = PaperShade
val Line = Rule

// Legacy / cross-palette aliases
val Parchment = Ink
val Ash = Graphite
val AshFaint = GraphiteFaint
val Ember = Indigo
val EmberDim = IndigoDim
val Brass = Ochre
val BrassDim = OchreDim
val Sage = Moss
val SageDim = MossDim

// ==========================================
// Dark Variant Tokens
// ==========================================
val DarkFog = Color(0xFF191B1F)
val DarkPaper = Color(0xFF22252B)
val DarkPaperShade = Color(0xFF2B2E36)
val DarkRule = Color(0xFF3B3F4A)

val DarkInk = Color(0xFFEDEBE6)
val DarkGraphite = Color(0xFFA8A59D)
val DarkGraphiteFaint = Color(0xFF686660)

val DarkIndigo = Color(0xFF8FA1CA)
val DarkIndigoDim = Color(0x2E8FA1CA)
val DarkOchre = Color(0xFFDEB062)
val DarkOchreDim = Color(0x2EDEB062)
val DarkMoss = Color(0xFF88A882)
val DarkMossDim = Color(0x2E88A882)

data class TaskPulseColorPalette(
    val fog: Color,
    val paper: Color,
    val paperShade: Color,
    val rule: Color,
    val ink: Color,
    val graphite: Color,
    val graphiteFaint: Color,
    val indigo: Color,
    val indigoDim: Color,
    val ochre: Color,
    val ochreDim: Color,
    val moss: Color,
    val mossDim: Color,
    // Semantic mappings
    val background: Color = fog,
    val surface: Color = paper,
    val surfaceVariant: Color = paperShade,
    val border: Color = rule,
    val panel: Color = paper,
    val panelRaised: Color = paperShade,
    val line: Color = rule,
    val textHi: Color = ink,
    val textLo: Color = graphite,
    val textFaint: Color = graphiteFaint,
    val pulse: Color = indigo,
    val pulseDim: Color = indigoDim,
    val amber: Color = ochre,
    val amberDim: Color = ochreDim,
    val teal: Color = moss,
    val tealDim: Color = mossDim
)

val PaperLightTaskPulsePalette = TaskPulseColorPalette(
    fog = Fog,
    paper = Paper,
    paperShade = PaperShade,
    rule = Rule,
    ink = Ink,
    graphite = Graphite,
    graphiteFaint = GraphiteFaint,
    indigo = Indigo,
    indigoDim = IndigoDim,
    ochre = Ochre,
    ochreDim = OchreDim,
    moss = Moss,
    mossDim = MossDim
)

val PaperDarkTaskPulsePalette = TaskPulseColorPalette(
    fog = DarkFog,
    paper = DarkPaper,
    paperShade = DarkPaperShade,
    rule = DarkRule,
    ink = DarkInk,
    graphite = DarkGraphite,
    graphiteFaint = DarkGraphiteFaint,
    indigo = DarkIndigo,
    indigoDim = DarkIndigoDim,
    ochre = DarkOchre,
    ochreDim = DarkOchreDim,
    moss = DarkMoss,
    mossDim = DarkMossDim
)

val LightTaskPulsePalette = PaperLightTaskPulsePalette
val DarkTaskPulsePalette = PaperDarkTaskPulsePalette

val LocalTaskPulseColors = staticCompositionLocalOf { PaperLightTaskPulsePalette }