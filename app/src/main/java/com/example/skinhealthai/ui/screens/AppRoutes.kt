//package com.example.skinhealthai.ui.screens
//
//object AppRoutes {
//    const val SIGNUP = "signup"
//    const val LOGIN = "login"
//    const val HOME = "home"
//    const val PATIENT_LIST = "patient_list"
//    const val ANALYSIS_HISTORY = "analysis_history"
//
//    // Rota base para registro/edição de paciente
//    const val PATIENT_REGISTER_BASE = "patient_register"
//    // Rota para edição de paciente com ID opcional
//    const val PATIENT_REGISTER_WITH_ID = "$PATIENT_REGISTER_BASE?patientId={patientId}"
//
//
//    // --- ROTAS DO PRONTUÁRIO UNIFICADAS ---
//    // Rota da tela de prontuário base
//    const val PATIENT_RECORD_BASE = "patient_record_screen" // Nome base para a tela
//
//    // Rota completa do prontuário com parâmetros opcionais (patientId e consultationId)
//    // Ex: patient_record_screen?patientId=9             (para visualizar prontuário do paciente 9)
//    // Ex: patient_record_screen?patientId=9&consultationId=15 (para editar consulta 15 do paciente 9)
//    const val PATIENT_RECORD_ROUTE = "$PATIENT_RECORD_BASE?patientId={patientId}&consultationId={consultationId}"
//    // Note que photoUri não é mais necessário aqui, já que a tela carrega da consulta
//    // Se você ainda precisa passar photoUri por algum motivo, adicione-o aqui, mas lembre-se que ele não define o modo de edição.
//
//
//    const val IMAGE_GALLERY = "image_gallery"
//    const val SCAN_IMAGE_BASE = "scan_image"
//    const val SCAN_IMAGE_WITH_INDEX = "$SCAN_IMAGE_BASE/{index}"
//    // PATIENT_REGISTER é redundante se PATIENT_REGISTER_BASE já existe e é usado para a tela
//    // const val PATIENT_REGISTER = "patient_register" // Pode ser removido se não houver outro uso direto
//
//    // Rota da tela de consulta, que agora espera o patientId como segmento de caminho
//    const val CONSULTATION_SCREEN_BASE = "consultation_screen"
//    const val CONSULTATION_SCREEN_WITH_PATIENT_ID = "$CONSULTATION_SCREEN_BASE/{patientId}"
//}