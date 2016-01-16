#!/usr/bin/env python

# Set this variable to "threading", "eventlet" or "gevent" to test the
# different async modes, or leave it set to None for the application to choose
# the best option based on available packages.
async_mode = None

if async_mode is None:
    try:
        import eventlet
        async_mode = 'eventlet'
    except ImportError:
        pass

    if async_mode is None:
        try:
            from gevent import monkey
            async_mode = 'gevent'
        except ImportError:
            pass

    if async_mode is None:
        async_mode = 'threading'

    print('async_mode is ' + async_mode)

# monkey patching is necessary because this application uses a background
# thread
if async_mode == 'eventlet':
    import eventlet
    eventlet.monkey_patch()
elif async_mode == 'gevent':
    from gevent import monkey
    monkey.patch_all()

import time
from threading import Thread
from flask import Flask, render_template, session, request
from flask_socketio import SocketIO, emit, join_room, leave_room, \
    close_room, rooms, disconnect

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'
socketio = SocketIO(app, async_mode=async_mode)

MESSAGES = {}

@app.route('/')
def index():
    return render_template('index.html')

# As a user joins the room
@socketio.on('join', namespace='/solo')
def join(data):
    room = data['room']
    username = data['username']
    join_room(room)
    print('joined room')

    # Initialize the record for this room if room is new
    global MESSAGES
    if room not in MESSAGES.keys():
        MESSAGES[room] = []

    # Emit every messages
    emit('joined room', {
        'username': username,
        'room': room,
        'messages': MESSAGES[room]
        },
        room=room)

# New message in a room
@socketio.on('room message', namespace='/solo')
def room_message(data):
    room = data['room']
    username = data['username']
    message = data['message']

    # Append the message to the record of this room
    global MESSAGES
    if room in MESSAGES.keys():
        MESSAGES[room].append(message)
    else:
        MESSAGES[room] = [message]

    # Emit every messages of this room
    emit('send room message', {
        'username': username,
        'room': room,
        'message': message
        },
        room=room)

# As a user leaves the room
@socketio.on('leave', namespace='/solo')
def leave(data):
    room = data['room']
    username = data['username']
    leave_room(room)
    emit('left room', {
        'username': username,
        'room': room
        },
        room=room)

# As a user disconnect
@socketio.on('disconnect request', namespace='solo')
def disconnect_request():
    emit('disconnected', {
        'message': 'Disconnected!'
        })
    disconnect()

@socketio.on('disconnect', namespace='/solo')
def test_disconnect():
    print('Client disconnected', request.sid)

if __name__ == '__main__':
    socketio.run(app, debug=True, host='0.0.0.0')
