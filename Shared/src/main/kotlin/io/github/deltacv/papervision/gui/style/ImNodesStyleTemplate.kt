/*
 * PaperVision
 * Copyright (C) 2024 Sebastian Erives, deltacv

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.deltacv.papervision.gui.style

interface ImNodesStyleTemplate : Style {
    val nodeBackground: Int
    val nodeBackgroundHovered: Int
    val nodeBackgroundSelected: Int
    val nodeOutline: Int

    val titleBar: Int
    val titleBarHovered: Int
    val titleBarSelected: Int

    val link: Int
    val linkHovered: Int
    val linkSelected: Int

    val pin: Int
    val pinHovered: Int

    val boxSelector: Int
    val boxSelectorOutline: Int
}