let canvas, render, width, height;

const apiKey = "observe0";

/* add rounded rect method to canvas */
CanvasRenderingContext2D.prototype.roundRect = function (x, y, w, h, r) {
  if (w < 2 * r) r = w / 2;
  if (h < 2 * r) r = h / 2;
  this.beginPath();
  this.moveTo(x+r, y);
  this.arcTo(x+w, y,   x+w, y+h, r);
  this.arcTo(x+w, y+h, x,   y+h, r);
  this.arcTo(x,   y+h, x,   y,   r);
  this.arcTo(x,   y,   x+w, y,   r);
  this.closePath();
  return this;
}

function pad(n, width, z) {
  z = z || '0';
  n = n + '';
  return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

/* setup a full page canvas */
window.onload = () => {
    canvas = document.getElementById("canvas");
    render = canvas.getContext("2d");

    const resize = () => {
        width = window.innerWidth;
        height = window.innerHeight;
        canvas.width = width;
        canvas.height = height;
    };

    resize();
    window.onresize = resize;
}

function playerToColor(player) {
    return ["CornflowerBlue", "Crimson", "DarkOrange", "Olive"][player];
}

function drawProgress(progress) {
    const moved = progress.moved;
    const dead = progress.dead;

    render.fillStyle = "white";
    render.fillRect(0, 0, width, 55);

    for(let i = 0; i < 4; i++) {
        render.fillStyle = playerToColor(i);
        render.roundRect(i * width/4 + 5, 5, width/4 - 10, 45, 5).fill();

        render.font = "15px monospace";
        render.textAlign = "center";
        render.fillStyle = "white";
        render.fillText(`Player ${i}`, (i + 0.5) * width/4, 23, width/4 - 20);
        render.fillText((dead[i] ? "Dead" : "Alive") + " - " + (moved[i] ? "Moved" : "Waiting"),(i + 0.5) * width/4, 43, width/4 - 20);

        if(dead[i]) {
            render.beginPath()
            render.moveTo(i*width/4 + 5, 5);
            render.lineTo((i+1)*width/4 - 5, 50);
            render.stroke();
            render.beginPath();
            render.moveTo((i+1)*width/4 - 5, 5);
            render.lineTo(i*width/4 + 5, 50);
            render.stroke();

        }
    }
}

function drawBoard(board, x, y, w, h) {
    render.fillStyle = "white";
    render.fillRect(x, y, w, h);

    if(w > h) {
        drawBoardSquare(board, x + (w - h)/2, y, h);
    } else {
        drawBoardSquare(board, x, y + (h - w)/2, w);
    }
}

function drawBoardSquare(board, xOff, yOff, size) {
    for(let x = 0; x < 25; x++) {
        for(let y = 0; y < 25; y++) {
            render.strokeRect(xOff + x*(size/25), yOff + y*(size/25), size/25, size/25);

            if(board.board[x][y] == -1) continue;
            render.fillStyle = playerToColor(board.board[x][y]);

            render.fillRect(xOff + x*(size/25), yOff + y*(size/25), size/25, size/25);
        }
    }

    /* draw food */
    render.fillStyle = "HotPink";
    render.roundRect(xOff + board.food[0]*(size/25), yOff+board.food[1]*(size/25), size/25, size/25, 10).fill();
}

async function main() {
    const progress = await JSON.parse(await (await fetch(`/api/progress?key=${apiKey}`)).text());
    if(progress != null) drawProgress(progress);
    const board = await JSON.parse(await (await fetch(`/api/board?key=${apiKey}`)).text());
    if(board != null) drawBoard(board, 5, 60, width - 10, height - 70);
}

window.setInterval(main, 500);