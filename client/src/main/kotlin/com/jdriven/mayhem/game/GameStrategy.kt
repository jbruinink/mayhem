package com.jdriven.mayhem.game

import ninja.robbert.mayhem.api.StatusMessage

interface GameStrategy {
    fun createResponse(msg: StatusMessage): Action?
}