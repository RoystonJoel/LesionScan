package org.lesionscan.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform