import os
import subprocess
from django.http import JsonResponse, HttpResponseRedirect
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.urls import reverse
from django.shortcuts import render

def home(request):
    file_content = None

    if request.method == 'POST':
        pattern = request.POST.get('pattern', '').strip()
        where_clause = request.POST.get('where', '').strip()
        and_clause = request.POST.get('and', '').strip()
        within_clause = request.POST.get('within', '').strip()

        if pattern and where_clause and and_clause and within_clause:
            file_content = f"PATTERN SEQ ({pattern})\nWHERE {where_clause}\nAND {and_clause}\nWITHIN {within_clause}"

            # Define the directory and file path
            directory_path = '/home/kiriakos/Documents/SASE HOME MSC/build'
            file_path = os.path.join(directory_path, 'test.query')

            # Ensure the directory exists
            if not os.path.exists(directory_path):
                os.makedirs(directory_path)

            # Write the content to the file
            with open(file_path, 'w') as file:
                file.write(file_content)

            # Read the content from the file
            with open(file_path, 'r') as file:
                file_content = file.read()

    return render(request, 'home.html', {'file_content': file_content})

@csrf_exempt
@require_POST
def initiate_sase(request):
    try:
        # Absolute path to the directory containing sase.jar, test.query, and test.stream
        sase_dir = '/home/kiriakos/Documents/SASE HOME MSC/build'
        jar_path = 'sase.jar'
        result = subprocess.run(
            ['java', '-jar', jar_path, 'test.query', 'test.stream'],
            cwd=sase_dir,
            capture_output=True,
            text=True
        )
        request.session['sase_output'] = result.stdout
        request.session['sase_error'] = result.stderr
        return JsonResponse({'status': 'success', 'redirect_url': reverse('sase_result')})
    except Exception as e:
        return JsonResponse({'status': 'error', 'error': str(e)})

def sase_result(request):
    output = request.session.get('sase_output', 'No output available')
    error = request.session.get('sase_error', '')
    return render(request, 'sase_result.html', {'output': output, 'error': error})

@csrf_exempt
@require_POST
def run_docker_compose(request):
    try:
        compose_file_dir = '/home/kiriakos/Desktop/ergasia_no_db'
        sudo_password = 'kiriakos'  # Replace this with the actual sudo password

        # Command to run
        command = ['sudo', '-S', 'docker-compose', '-f', 'docker-compose.yml', 'up', '-d']

        # Use Popen to run the command and provide the password
        process = subprocess.Popen(command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=compose_file_dir, text=True)
        stdout, stderr = process.communicate(sudo_password + '\n')

        if process.returncode == 0:
            return JsonResponse({'status': 'success', 'message': 'Docker Compose started successfully', 'output': stdout})
        else:
            return JsonResponse({'status': 'error', 'message': stderr})
    except Exception as e:
        return JsonResponse({'status': 'error', 'message': str(e)})