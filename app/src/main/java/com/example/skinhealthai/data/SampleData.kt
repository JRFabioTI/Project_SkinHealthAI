package com.example.skinhealthai.data

import com.example.skinhealthai.data.model.Patient
import com.example.skinhealthai.data.model.AnalysisHistoryItem

val samplePatients = listOf(
    Patient(id = 1, name = "João Silva", email = "joao.s@example.com", birthDate = "1986-05-15"),
    Patient(id = 2, name = "Maria Souza", email = "maria.s@example.com", birthDate = "1995-11-20"),
    Patient(id = 3, name = "Ana Oliveira", email = "ana.o@example.com", birthDate = "1978-01-01"),
    Patient(id = 4, name = "Carlos Santos", email = "carlos.s@example.com", birthDate = "1969-03-25"),
    Patient(id = 5, name = "Beatriz Costa", email = "beatriz.c@example.com", birthDate = "2002-07-10"),
    Patient(id = 6, name = "Pedro Almeida", email = "pedro.a@example.com", birthDate = "1964-08-05"),
    Patient(id = 7, name = "Sofia Mendes", email = "sofia.m@example.com", birthDate = null),
)

val sampleAnalysisHistory = listOf(
    AnalysisHistoryItem("analise_001", "João Silva", "10/05/2024", "Melanoma suspeito", "Alto Risco"),
    AnalysisHistoryItem("analise_002", "Maria Souza", "12/05/2024", "Nevus comum", "Baixo Risco"),
    AnalysisHistoryItem("analise_003", "Ana Oliveira", "15/05/2024", "Queratose actínica", "Médio Risco"),
    AnalysisHistoryItem("analise_004", "Carlos Santos", "01/05/2024", "Eczema", "Baixo Risco"),
    AnalysisHistoryItem("analise_005", "Beatriz Costa", "08/04/2024", "Dermatite de contato", "Baixo Risco"),
)
